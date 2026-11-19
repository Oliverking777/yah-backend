-- =============================================================
-- V32 – Purchasing: one document, three statuses
-- =============================================================
-- Before: a purchase (DRAFT -> ORDERED -> PARTIALLY_RECEIVED / RECEIVED -> CLOSED, or CANCELLED) received through
-- separate goods receipts (DRAFT -> CONFIRMED / CANCELLED), with discrepancy records to resolve.
-- Now:    a purchase is SUBMITTED when it is ordered, then CONFIRMED (the one delivery is counted on its lines and
--         the accepted goods go into stock) or CANCELLED. Rejected or missing goods stay on the lines and set
--         has_discrepancy. Goods receipts and discrepancy records are gone.
--
-- Existing data:
--   DRAFT                                  -> CANCELLED (it was never ordered)
--   ORDERED                                -> SUBMITTED (draft receipts against it are dropped)
--   PARTIALLY_RECEIVED, RECEIVED, CLOSED   -> CONFIRMED with what the confirmed receipts accepted and rejected
--                                             (a missing remainder is a shortfall); a CLOSED purchase that never
--                                             received anything -> CANCELLED
--   For ordered purchases, created_by / created_at take the person who ordered and when.
--   No stock moves here: what was accepted is already in stock.
-- =============================================================

ALTER TABLE public.purchases ADD COLUMN has_discrepancy BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE public.purchases ADD COLUMN delivery_note VARCHAR(50);
ALTER TABLE public.purchases ADD COLUMN confirmed_by UUID;
ALTER TABLE public.purchases ADD COLUMN confirmed_at TIMESTAMPTZ;
ALTER TABLE public.purchases DROP CONSTRAINT ck_purchases_status;

ALTER TABLE public.purchase_items DROP CONSTRAINT ck_purchase_items_received;
ALTER TABLE public.purchase_items RENAME COLUMN received_quantity TO accepted_quantity;
ALTER TABLE public.purchase_items ALTER COLUMN accepted_quantity DROP NOT NULL;
ALTER TABLE public.purchase_items ALTER COLUMN accepted_quantity DROP DEFAULT;
ALTER TABLE public.purchase_items ADD COLUMN rejected_quantity INT;
ALTER TABLE public.purchase_items ADD COLUMN rejection_type VARCHAR(20);
ALTER TABLE public.purchase_items ADD COLUMN discrepancy_reason VARCHAR(500);

-- ---------- what the confirmed receipts say, per line and per purchase ----------
CREATE TEMP TABLE v32_lines AS
SELECT gi.purchase_item_id,
       SUM(gi.quantity_accepted) AS accepted,
       SUM(gi.quantity_rejected) AS rejected,
       (ARRAY_AGG(gi.rejection_type ORDER BY g.confirmed_at DESC) FILTER (WHERE gi.quantity_rejected > 0))[1] AS rejection_type,
       STRING_AGG(gi.rejection_reason, '; ' ORDER BY g.confirmed_at) FILTER (WHERE gi.rejection_reason IS NOT NULL) AS reasons
  FROM public.goods_receipt_items gi
  JOIN public.goods_receipts g ON g.goods_receipt_id = gi.goods_receipt_id
 WHERE g.receipt_status = 'CONFIRMED'
 GROUP BY gi.purchase_item_id;

CREATE TEMP TABLE v32_receipts AS
SELECT purchase_id,
       MAX(confirmed_at) AS confirmed_at,
       (ARRAY_AGG(confirmed_by ORDER BY confirmed_at DESC))[1] AS confirmed_by,
       LEFT(STRING_AGG(delivery_note, ', ' ORDER BY confirmed_at) FILTER (WHERE delivery_note IS NOT NULL), 50) AS delivery_note
  FROM public.goods_receipts
 WHERE receipt_status = 'CONFIRMED'
 GROUP BY purchase_id;

-- ---------- statuses ----------
-- the person who ordered and when become the creator and the creation time
UPDATE public.purchases SET created_by = ordered_by, created_at = ordered_at
 WHERE ordered_at IS NOT NULL AND ordered_by IS NOT NULL;

UPDATE public.purchases
   SET purchase_status = 'CANCELLED',
       cancel_reason = COALESCE(cancel_reason, 'Closed by the update: it was never ordered.'),
       cancelled_by = COALESCE(cancelled_by, created_by),
       cancelled_at = COALESCE(cancelled_at, NOW())
 WHERE purchase_status = 'DRAFT';

UPDATE public.purchases p
   SET purchase_status = 'CANCELLED',
       cancel_reason = LEFT('Closed before any delivery: ' || COALESCE(p.close_reason, 'no reason given'), 500),
       cancelled_by = COALESCE(p.closed_by, p.created_by),
       cancelled_at = COALESCE(p.closed_at, NOW())
 WHERE p.purchase_status = 'CLOSED'
   AND NOT EXISTS (SELECT 1 FROM v32_receipts r WHERE r.purchase_id = p.purchase_id);

UPDATE public.purchases SET purchase_status = 'SUBMITTED' WHERE purchase_status = 'ORDERED';

UPDATE public.purchases p
   SET purchase_status = 'CONFIRMED',
       confirmed_at = COALESCE(r.confirmed_at, p.closed_at, p.updated_at),
       confirmed_by = COALESCE(r.confirmed_by, p.closed_by, p.created_by),
       delivery_note = r.delivery_note
  FROM public.purchases q
  LEFT JOIN v32_receipts r ON r.purchase_id = q.purchase_id
 WHERE q.purchase_id = p.purchase_id
   AND p.purchase_status IN ('PARTIALLY_RECEIVED', 'RECEIVED', 'CLOSED');

-- ---------- lines of confirmed purchases: what was accepted, rejected, and why it differs ----------
UPDATE public.purchase_items i
   SET accepted_quantity = l.accepted,
       rejected_quantity = l.rejected,
       rejection_type = CASE WHEN l.rejected > 0 THEN l.rejection_type END,
       discrepancy_reason = l.reasons
  FROM v32_lines l, public.purchases p
 WHERE l.purchase_item_id = i.purchase_item_id
   AND p.purchase_id = i.purchase_id AND p.purchase_status = 'CONFIRMED';

-- lines that never received anything
UPDATE public.purchase_items i
   SET accepted_quantity = 0, rejected_quantity = 0, rejection_type = NULL, discrepancy_reason = NULL
  FROM public.purchases p
 WHERE p.purchase_id = i.purchase_id AND p.purchase_status = 'CONFIRMED'
   AND NOT EXISTS (SELECT 1 FROM v32_lines l WHERE l.purchase_item_id = i.purchase_item_id);

-- a missing remainder is a shortfall: say so in the reason
UPDATE public.purchase_items i
   SET discrepancy_reason = LEFT(CONCAT_WS('; ', i.discrepancy_reason,
                                 'Short delivery' || COALESCE(': ' || p.close_reason, '')), 500)
  FROM public.purchases p
 WHERE p.purchase_id = i.purchase_id AND p.purchase_status = 'CONFIRMED'
   AND i.accepted_quantity + i.rejected_quantity < i.ordered_quantity;

-- nothing has arrived on the others yet
UPDATE public.purchase_items i SET accepted_quantity = NULL
  FROM public.purchases p
 WHERE p.purchase_id = i.purchase_id AND p.purchase_status <> 'CONFIRMED';

UPDATE public.purchases p SET has_discrepancy = TRUE
 WHERE p.purchase_status = 'CONFIRMED'
   AND EXISTS (SELECT 1 FROM public.purchase_items i
                WHERE i.purchase_id = p.purchase_id
                  AND (i.rejected_quantity > 0 OR i.accepted_quantity < i.ordered_quantity));

-- ---------- what no longer exists ----------
DROP TABLE public.purchase_discrepancies;
DROP TABLE public.goods_receipt_items;
DROP TABLE public.goods_receipts;
DROP SEQUENCE public.goods_receipt_number_seq;
DROP TABLE v32_lines;
DROP TABLE v32_receipts;

ALTER TABLE public.purchases DROP COLUMN ordered_by;
ALTER TABLE public.purchases DROP COLUMN ordered_at;
ALTER TABLE public.purchases DROP COLUMN closed_by;
ALTER TABLE public.purchases DROP COLUMN closed_at;
ALTER TABLE public.purchases DROP COLUMN close_reason;

-- ---------- rules ----------
ALTER TABLE public.purchases ADD CONSTRAINT ck_purchases_status
    CHECK (purchase_status IN ('SUBMITTED', 'CONFIRMED', 'CANCELLED'));
ALTER TABLE public.purchases ADD CONSTRAINT ck_purchases_confirm_recorded
    CHECK (purchase_status <> 'CONFIRMED' OR (confirmed_by IS NOT NULL AND confirmed_at IS NOT NULL));
ALTER TABLE public.purchases ADD CONSTRAINT ck_purchases_cancel_recorded
    CHECK (purchase_status <> 'CANCELLED'
           OR (cancelled_by IS NOT NULL AND cancelled_at IS NOT NULL AND cancel_reason IS NOT NULL));
ALTER TABLE public.purchases ADD CONSTRAINT ck_purchases_discrepancy_confirmed
    CHECK (NOT has_discrepancy OR purchase_status = 'CONFIRMED');

ALTER TABLE public.purchase_items ADD CONSTRAINT ck_purchase_items_receipt_together
    CHECK ((accepted_quantity IS NULL) = (rejected_quantity IS NULL));
ALTER TABLE public.purchase_items ADD CONSTRAINT ck_purchase_items_accepted
    CHECK (accepted_quantity IS NULL OR accepted_quantity BETWEEN 0 AND ordered_quantity);
ALTER TABLE public.purchase_items ADD CONSTRAINT ck_purchase_items_rejected
    CHECK (rejected_quantity IS NULL OR rejected_quantity >= 0);
ALTER TABLE public.purchase_items ADD CONSTRAINT ck_purchase_items_rejection
    CHECK (COALESCE(rejected_quantity, 0) = 0 OR rejection_type IS NOT NULL);
ALTER TABLE public.purchase_items ADD CONSTRAINT ck_purchase_items_reason
    CHECK (accepted_quantity IS NULL
           OR (rejected_quantity = 0 AND accepted_quantity = ordered_quantity)
           OR discrepancy_reason IS NOT NULL);

CREATE INDEX idx_purchases_status_created ON public.purchases (purchase_status, created_at);
CREATE INDEX idx_purchases_confirmed_at ON public.purchases (confirmed_at) WHERE purchase_status = 'CONFIRMED';

COMMENT ON TABLE public.purchases IS 'Purchases from suppliers: SUBMITTED (ordered), CONFIRMED (delivered and counted) or CANCELLED. supplier_id, destination_location_id and user ids belong to other modules: plain UUIDs, no foreign keys.';
COMMENT ON COLUMN public.purchases.has_discrepancy IS 'Set at confirmation: goods were rejected or are missing on at least one line. Only for CONFIRMED.';
COMMENT ON COLUMN public.purchases.created_at IS 'When the goods were ordered.';
COMMENT ON COLUMN public.purchase_items.accepted_quantity IS 'Accepted into stock, in the line''s unit. Null until the purchase is confirmed.';
COMMENT ON COLUMN public.purchase_items.discrepancy_reason IS 'Why the line differs from the order (rejected or missing goods). Required when it does.';
