-- =============================================================
-- V31 – Transfer: three statuses only
-- =============================================================
-- Before: DRAFT -> SUBMITTED -> DISPATCHED -> RECEIVED / RECEIVED_WITH_DISCREPANCY, CANCELLED before dispatch.
-- Now:    SUBMITTED (sent, in transit) -> RECEIVED or CANCELLED.
--   * Creating a transfer sends it: TRANSFER_OUT at the source happens at creation.
--   * Damage or shortfall at receipt keeps the status RECEIVED and sets has_discrepancy (details on the lines).
--   * Cancelling a SUBMITTED transfer returns the goods to the source (TRANSFER_IN at the source).
--
-- Existing data:
--   DISPATCHED                  -> SUBMITTED (goods already left, same meaning)
--   RECEIVED_WITH_DISCREPANCY   -> RECEIVED with has_discrepancy = true
--   DRAFT, SUBMITTED (old)      -> CANCELLED: no stock ever left for them, so they cannot become
--                                  "in transit" without a TRANSFER_OUT. A CANCELLED event is added.
--   For sent transfers, created_by / created_at take the dispatcher and the dispatch time,
--   and each line's quantity becomes the dispatched quantity (lines dispatched at 0 are removed).
--   Events are rewritten to the new types; the append-only trigger is paused for this migration only.
-- =============================================================

ALTER TABLE public.transfer_events DISABLE TRIGGER trg_transfer_events_append_only;

-- ---------- old constraints that mention removed statuses or columns ----------
ALTER TABLE public.transfers DROP CONSTRAINT ck_transfers_status;
ALTER TABLE public.transfers DROP CONSTRAINT ck_transfers_dispatch_recorded;
ALTER TABLE public.transfers DROP CONSTRAINT ck_transfers_receipt_recorded;
ALTER TABLE public.transfer_items DROP CONSTRAINT ck_transfer_items_dispatched;
ALTER TABLE public.transfer_items DROP CONSTRAINT ck_transfer_items_receipt_total;
ALTER TABLE public.transfer_events DROP CONSTRAINT ck_transfer_events_type;

-- ---------- transfers that never left: close them ----------
WITH closed AS (
    UPDATE public.transfers
       SET transfer_status = 'CANCELLED',
           cancelled_by    = created_by,
           cancelled_at    = now(),
           cancel_reason   = 'Closed by system: never sent before transfers became send-on-create',
           updated_at      = now()
     WHERE transfer_status IN ('DRAFT', 'SUBMITTED')
    RETURNING transfer_id, cancelled_by, cancelled_at, cancel_reason
)
INSERT INTO public.transfer_events (event_id, transfer_id, event_type, from_status, to_status, performed_by,
                                    occurred_at, note)
SELECT gen_random_uuid(), transfer_id, 'CANCELLED', 'SUBMITTED', 'CANCELLED', cancelled_by, cancelled_at,
       cancel_reason
  FROM closed;

-- ---------- sent transfers: the dispatcher is the sender ----------
UPDATE public.transfers
   SET created_by = dispatched_by,
       created_at = dispatched_at
 WHERE dispatched_at IS NOT NULL;

UPDATE public.transfers SET transfer_status = 'SUBMITTED' WHERE transfer_status = 'DISPATCHED';
-- the receipt outcome moves out of the status into its own column
ALTER TABLE public.transfers ADD COLUMN has_discrepancy BOOLEAN NOT NULL DEFAULT false;
UPDATE public.transfers
   SET transfer_status = 'RECEIVED', has_discrepancy = true
 WHERE transfer_status = 'RECEIVED_WITH_DISCREPANCY';

-- ---------- lines: quantity = what was sent ----------
DELETE FROM public.transfer_items WHERE dispatched_quantity = 0;
UPDATE public.transfer_items
   SET requested_quantity = dispatched_quantity
 WHERE dispatched_quantity IS NOT NULL;

-- ---------- events: rewrite history to the new steps ----------
DELETE FROM public.transfer_events WHERE event_type IN ('CREATED', 'SUBMITTED');
UPDATE public.transfer_events
   SET event_type = 'SUBMITTED', from_status = NULL, to_status = 'SUBMITTED'
 WHERE event_type = 'DISPATCHED';
UPDATE public.transfer_events
   SET from_status = 'SUBMITTED', to_status = 'RECEIVED'
 WHERE event_type = 'RECEIVED';
UPDATE public.transfer_events
   SET from_status = 'SUBMITTED'
 WHERE event_type = 'CANCELLED';

-- ---------- drop what no longer exists ----------
ALTER TABLE public.transfers DROP COLUMN dispatched_by;
ALTER TABLE public.transfers DROP COLUMN submitted_at;
ALTER TABLE public.transfers DROP COLUMN dispatched_at;
ALTER TABLE public.transfer_items DROP COLUMN dispatched_quantity;
ALTER TABLE public.transfer_items RENAME COLUMN requested_quantity TO quantity;
ALTER TABLE public.transfer_items RENAME CONSTRAINT ck_transfer_items_requested TO ck_transfer_items_quantity;

-- ---------- new constraints ----------
ALTER TABLE public.transfers ADD CONSTRAINT ck_transfers_status
    CHECK (transfer_status IN ('SUBMITTED', 'RECEIVED', 'CANCELLED'));
ALTER TABLE public.transfers ADD CONSTRAINT ck_transfers_receipt_recorded
    CHECK (transfer_status <> 'RECEIVED' OR (received_by IS NOT NULL AND received_at IS NOT NULL));
ALTER TABLE public.transfers ADD CONSTRAINT ck_transfers_discrepancy_received
    CHECK (NOT has_discrepancy OR transfer_status = 'RECEIVED');
CREATE INDEX idx_transfers_discrepancy_received_at
    ON public.transfers (received_at DESC) WHERE has_discrepancy;
ALTER TABLE public.transfer_items ADD CONSTRAINT ck_transfer_items_receipt_total
    CHECK (COALESCE(received_quantity, 0) + COALESCE(damaged_quantity, 0) <= quantity);
ALTER TABLE public.transfer_events ADD CONSTRAINT ck_transfer_events_type
    CHECK (event_type IN ('SUBMITTED', 'RECEIVED', 'CANCELLED'));
ALTER TABLE public.transfer_events ADD CONSTRAINT ck_transfer_events_status
    CHECK ((from_status IS NULL OR from_status = 'SUBMITTED')
       AND to_status IN ('SUBMITTED', 'RECEIVED', 'CANCELLED'));

ALTER TABLE public.transfer_events ENABLE TRIGGER trg_transfer_events_append_only;

COMMENT ON COLUMN public.transfers.transfer_status IS 'SUBMITTED (sent, in transit), RECEIVED or CANCELLED. The last two are final.';
COMMENT ON COLUMN public.transfers.has_discrepancy IS 'Receipt outcome, set once at receipt: true when at least one line arrived damaged or short. Always false unless RECEIVED.';
COMMENT ON COLUMN public.transfers.created_by IS 'The sender: creating a transfer sends it.';
COMMENT ON COLUMN public.transfers.created_at IS 'When the goods were sent (TRANSFER_OUT at the source).';
COMMENT ON COLUMN public.transfer_items.quantity IS 'Quantity sent, in the line''s unit. Base quantity = quantity x conversion_factor.';
