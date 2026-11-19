-- =============================================================
-- V21 – Transfer module
-- =============================================================
-- Moves products between two locations. Stock changes only through Inventory: TRANSFER_OUT at dispatch,
-- TRANSFER_IN (good quantity only) at receipt. Goods in transit are not stored: they are derived from
-- the transfers in DISPATCHED status.
-- No foreign keys to other modules: location, product, unit and user ids are plain UUIDs.
-- Permissions (TRANSFER_CREATE, TRANSFER_READ, TRANSFER_CONFIRM) already exist in V10.
-- Needs the Inventory module (V20): Transfer writes stock through it.
-- =============================================================

CREATE SEQUENCE public.transfer_number_seq START WITH 1 INCREMENT BY 1;

-- ---------- transfers ----------
CREATE TABLE public.transfers (
    transfer_id             UUID          NOT NULL,
    transfer_number         VARCHAR(20)   NOT NULL,
    source_location_id      UUID          NOT NULL,
    destination_location_id UUID          NOT NULL,
    transfer_status         VARCHAR(30)   NOT NULL,
    note                    VARCHAR(500),
    created_by              UUID          NOT NULL,
    dispatched_by           UUID,
    received_by             UUID,
    cancelled_by            UUID,
    cancel_reason           VARCHAR(500),
    created_at              TIMESTAMPTZ   NOT NULL,
    submitted_at            TIMESTAMPTZ,
    dispatched_at           TIMESTAMPTZ,
    received_at             TIMESTAMPTZ,
    cancelled_at            TIMESTAMPTZ,
    updated_at              TIMESTAMPTZ   NOT NULL,

    CONSTRAINT pk_transfers PRIMARY KEY (transfer_id),
    CONSTRAINT uq_transfers_transfer_number UNIQUE (transfer_number),
    CONSTRAINT ck_transfers_locations_differ CHECK (source_location_id <> destination_location_id),
    CONSTRAINT ck_transfers_status CHECK (transfer_status IN (
        'DRAFT', 'SUBMITTED', 'DISPATCHED', 'RECEIVED', 'RECEIVED_WITH_DISCREPANCY', 'CANCELLED')),
    -- a dispatched or received transfer always says who sent it and when
    CONSTRAINT ck_transfers_dispatch_recorded CHECK (
        transfer_status NOT IN ('DISPATCHED', 'RECEIVED', 'RECEIVED_WITH_DISCREPANCY')
        OR (dispatched_by IS NOT NULL AND dispatched_at IS NOT NULL)),
    CONSTRAINT ck_transfers_receipt_recorded CHECK (
        transfer_status NOT IN ('RECEIVED', 'RECEIVED_WITH_DISCREPANCY')
        OR (received_by IS NOT NULL AND received_at IS NOT NULL)),
    CONSTRAINT ck_transfers_cancel_recorded CHECK (
        transfer_status <> 'CANCELLED'
        OR (cancelled_by IS NOT NULL AND cancelled_at IS NOT NULL AND cancel_reason IS NOT NULL))
);

CREATE INDEX idx_transfers_source_status      ON public.transfers (source_location_id, transfer_status);
CREATE INDEX idx_transfers_destination_status ON public.transfers (destination_location_id, transfer_status);
CREATE INDEX idx_transfers_created_at         ON public.transfers (created_at DESC);

COMMENT ON TABLE  public.transfers IS 'A movement of products between two locations. No foreign keys to other modules: location ids and user ids belong to other modules.';
COMMENT ON COLUMN public.transfers.transfer_number IS 'System-generated TRF-000001 style number from transfer_number_seq. Never edited.';
COMMENT ON COLUMN public.transfers.transfer_status IS 'DRAFT, SUBMITTED, DISPATCHED, RECEIVED, RECEIVED_WITH_DISCREPANCY or CANCELLED. The last three are final.';

-- ---------- transfer_items ----------
CREATE TABLE public.transfer_items (
    transfer_item_id    UUID          NOT NULL,
    transfer_id         UUID          NOT NULL,
    product_id          UUID          NOT NULL,
    unit_id             UUID          NOT NULL,
    conversion_factor   INT           NOT NULL,
    requested_quantity  INT           NOT NULL,
    dispatched_quantity INT,
    received_quantity   INT,
    damaged_quantity    INT,
    discrepancy_reason  VARCHAR(500),

    CONSTRAINT pk_transfer_items PRIMARY KEY (transfer_item_id),
    CONSTRAINT fk_transfer_items_transfer FOREIGN KEY (transfer_id) REFERENCES public.transfers (transfer_id),
    CONSTRAINT uq_transfer_items_transfer_product UNIQUE (transfer_id, product_id),
    CONSTRAINT ck_transfer_items_factor CHECK (conversion_factor >= 1),
    CONSTRAINT ck_transfer_items_requested CHECK (requested_quantity >= 1),
    CONSTRAINT ck_transfer_items_dispatched CHECK (
        dispatched_quantity IS NULL OR (dispatched_quantity >= 0 AND dispatched_quantity <= requested_quantity)),
    CONSTRAINT ck_transfer_items_received CHECK (received_quantity IS NULL OR received_quantity >= 0),
    CONSTRAINT ck_transfer_items_damaged CHECK (damaged_quantity IS NULL OR damaged_quantity >= 0),
    CONSTRAINT ck_transfer_items_receipt_total CHECK (
        COALESCE(received_quantity, 0) + COALESCE(damaged_quantity, 0) <= COALESCE(dispatched_quantity, 0))
);

CREATE INDEX idx_transfer_items_transfer_id ON public.transfer_items (transfer_id);
CREATE INDEX idx_transfer_items_product_id  ON public.transfer_items (product_id);

COMMENT ON TABLE  public.transfer_items IS 'Lines of a transfer. product_id and unit_id belong to the Product module: plain UUIDs, no foreign keys.';
COMMENT ON COLUMN public.transfer_items.conversion_factor IS 'Copied from the product unit when the line is added, so a later change never alters an old transfer. Base quantity = quantity x factor.';
COMMENT ON COLUMN public.transfer_items.damaged_quantity IS 'Arrived damaged: not added to the destination stock.';

-- ---------- transfer_events (append-only) ----------
CREATE TABLE public.transfer_events (
    event_id      UUID         NOT NULL,
    transfer_id   UUID         NOT NULL,
    event_type    VARCHAR(30)  NOT NULL,
    from_status   VARCHAR(30),
    to_status     VARCHAR(30)  NOT NULL,
    performed_by  UUID         NOT NULL,
    occurred_at   TIMESTAMPTZ  NOT NULL,
    note          VARCHAR(500),

    CONSTRAINT pk_transfer_events PRIMARY KEY (event_id),
    CONSTRAINT fk_transfer_events_transfer FOREIGN KEY (transfer_id) REFERENCES public.transfers (transfer_id),
    CONSTRAINT ck_transfer_events_type CHECK (event_type IN (
        'CREATED', 'SUBMITTED', 'DISPATCHED', 'RECEIVED', 'CANCELLED'))
);

CREATE INDEX idx_transfer_events_transfer_time ON public.transfer_events (transfer_id, occurred_at);

-- Append-only at the database level, the same way as stock_movements: a trigger refuses UPDATE and DELETE.
CREATE FUNCTION public.transfer_events_append_only() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'transfer_events is append-only: % is not allowed', TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_transfer_events_append_only
    BEFORE UPDATE OR DELETE ON public.transfer_events
    FOR EACH ROW EXECUTE FUNCTION public.transfer_events_append_only();

COMMENT ON TABLE  public.transfer_events IS 'Every step of a transfer. APPEND-ONLY: trigger trg_transfer_events_append_only refuses UPDATE and DELETE. performed_by is a user id of another module.';
