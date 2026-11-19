-- Flyway migration V19: tables of the Inventory module (stock levels, stock ledger, stock adjustments).
-- Matches StockLevelJpaEntity, StockMovementJpaEntity and StockAdjustmentJpaEntity.
-- product_id, location_id and user ids come from other modules: no foreign keys, modules share ids, not tables.
-- Never edit this file after it has been applied; add a new Vn__... file for changes.
-- The permissions of this module (STOCK_*) were already created in V10.

CREATE SEQUENCE public.stock_adjustment_number_seq START WITH 1 INCREMENT BY 1;

-- ---------------------------------------------------------------
-- stock_levels: current quantity per product and location (base units)
-- ---------------------------------------------------------------
CREATE TABLE public.stock_levels (
    stock_level_id      UUID         NOT NULL,
    product_id          UUID         NOT NULL,
    location_id         UUID         NOT NULL,
    quantity            BIGINT       NOT NULL,
    low_stock_threshold BIGINT,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_stock_levels PRIMARY KEY (stock_level_id),
    CONSTRAINT uq_stock_levels_product_location UNIQUE (product_id, location_id),
    CONSTRAINT ck_stock_levels_quantity CHECK (quantity >= 0),
    CONSTRAINT ck_stock_levels_threshold CHECK (low_stock_threshold IS NULL OR low_stock_threshold >= 0)
);

CREATE INDEX idx_stock_levels_location ON public.stock_levels (location_id);

COMMENT ON TABLE  public.stock_levels          IS 'Current stock per product and location. product_id and location_id come from other modules: no foreign keys';
COMMENT ON COLUMN public.stock_levels.quantity IS 'Whole base units (pieces). Never negative; goods in transit are not counted here';
COMMENT ON COLUMN public.stock_levels.low_stock_threshold IS 'NULL means no threshold. Low stock when quantity <= threshold';

-- ---------------------------------------------------------------
-- stock_movements: the ledger. Append-only.
-- ---------------------------------------------------------------
CREATE TABLE public.stock_movements (
    movement_id    UUID         NOT NULL,
    product_id     UUID         NOT NULL,
    location_id    UUID         NOT NULL,
    movement_type  VARCHAR(20)  NOT NULL,
    quantity_delta BIGINT       NOT NULL,
    balance_after  BIGINT       NOT NULL,
    reference_type VARCHAR(20)  NOT NULL,
    reference_id   UUID         NOT NULL,
    performed_by   UUID         NOT NULL,
    occurred_at    TIMESTAMPTZ  NOT NULL,
    recorded_at    TIMESTAMPTZ  NOT NULL,
    note           VARCHAR(500),
    CONSTRAINT pk_stock_movements PRIMARY KEY (movement_id),
    CONSTRAINT ck_stock_movements_type CHECK (movement_type IN
        ('PURCHASE', 'TRANSFER_IN', 'SALE_RETURN', 'TRANSFER_OUT', 'SALE', 'ADJUSTMENT')),
    CONSTRAINT ck_stock_movements_reference_type CHECK (reference_type IN
        ('PURCHASE', 'TRANSFER', 'SALE', 'ADJUSTMENT')),
    CONSTRAINT ck_stock_movements_delta_not_zero CHECK (quantity_delta <> 0),
    CONSTRAINT ck_stock_movements_balance CHECK (balance_after >= 0),
    CONSTRAINT ck_stock_movements_sign CHECK (
        (movement_type IN ('PURCHASE', 'TRANSFER_IN', 'SALE_RETURN') AND quantity_delta > 0)
        OR (movement_type IN ('TRANSFER_OUT', 'SALE') AND quantity_delta < 0)
        OR movement_type = 'ADJUSTMENT'),
    CONSTRAINT ck_stock_movements_reference_match CHECK (
        (movement_type = 'PURCHASE' AND reference_type = 'PURCHASE')
        OR (movement_type IN ('TRANSFER_IN', 'TRANSFER_OUT') AND reference_type = 'TRANSFER')
        OR (movement_type IN ('SALE', 'SALE_RETURN') AND reference_type = 'SALE')
        OR (movement_type = 'ADJUSTMENT' AND reference_type = 'ADJUSTMENT'))
);

-- The real guarantee that a retried document line is applied once.
CREATE UNIQUE INDEX uq_stock_movements_reference ON public.stock_movements
    (reference_type, reference_id, product_id, location_id, movement_type);

CREATE INDEX idx_stock_movements_product_location ON public.stock_movements (product_id, location_id, recorded_at DESC);
CREATE INDEX idx_stock_movements_location_recorded ON public.stock_movements (location_id, recorded_at DESC);
CREATE INDEX idx_stock_movements_recorded_at ON public.stock_movements (recorded_at DESC);

-- Append-only: a mistake is corrected by a new movement, never by editing or deleting an old one.
CREATE FUNCTION public.stock_movements_append_only() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'stock_movements is append-only: % is not allowed', TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_stock_movements_append_only
    BEFORE UPDATE OR DELETE ON public.stock_movements
    FOR EACH ROW EXECUTE FUNCTION public.stock_movements_append_only();

COMMENT ON TABLE  public.stock_movements                IS 'Stock ledger, insert-only. Ids of product, location, user and the referenced document come from other modules: no foreign keys';
COMMENT ON COLUMN public.stock_movements.quantity_delta IS 'Signed base units: positive adds stock, negative removes it';
COMMENT ON COLUMN public.stock_movements.reference_id   IS 'Id of the transfer, sale (order), purchase or adjustment that caused the movement';
COMMENT ON COLUMN public.stock_movements.occurred_at    IS 'Business time (may be earlier for offline work); recorded_at is server time';

-- ---------------------------------------------------------------
-- stock_adjustments: requested by one user, decided by another
-- ---------------------------------------------------------------
CREATE TABLE public.stock_adjustments (
    adjustment_id     UUID         NOT NULL,
    adjustment_number VARCHAR(20)  NOT NULL,
    product_id        UUID         NOT NULL,
    location_id       UUID         NOT NULL,
    direction         VARCHAR(10)  NOT NULL,
    reason_category   VARCHAR(30)  NOT NULL,
    quantity          BIGINT       NOT NULL,
    note              VARCHAR(500),
    adjustment_status VARCHAR(20)  NOT NULL,
    created_by        UUID         NOT NULL,
    decided_by        UUID,
    decision_note     VARCHAR(500),
    before_quantity   BIGINT,
    after_quantity    BIGINT,
    created_at        TIMESTAMPTZ  NOT NULL,
    decided_at        TIMESTAMPTZ,
    updated_at        TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_stock_adjustments PRIMARY KEY (adjustment_id),
    CONSTRAINT uq_stock_adjustments_number UNIQUE (adjustment_number),
    CONSTRAINT ck_stock_adjustments_quantity CHECK (quantity >= 1),
    CONSTRAINT ck_stock_adjustments_direction CHECK (direction IN ('INCREASE', 'DECREASE')),
    CONSTRAINT ck_stock_adjustments_reason CHECK (reason_category IN
        ('DAMAGE', 'LOSS', 'SUPPLIER_RETURN', 'OPENING_STOCK', 'COUNT_CORRECTION', 'OTHER')),
    CONSTRAINT ck_stock_adjustments_reason_direction CHECK (
        (reason_category IN ('DAMAGE', 'LOSS', 'SUPPLIER_RETURN') AND direction = 'DECREASE')
        OR (reason_category = 'OPENING_STOCK' AND direction = 'INCREASE')
        OR reason_category IN ('COUNT_CORRECTION', 'OTHER')),
    CONSTRAINT ck_stock_adjustments_other_needs_note CHECK (
        reason_category <> 'OTHER' OR (note IS NOT NULL AND LENGTH(TRIM(note)) > 0)),
    CONSTRAINT ck_stock_adjustments_status CHECK (adjustment_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_stock_adjustments_decider_differs CHECK (decided_by IS NULL OR decided_by <> created_by),
    CONSTRAINT ck_stock_adjustments_decided CHECK (
        (adjustment_status = 'PENDING' AND decided_by IS NULL AND decided_at IS NULL)
        OR (adjustment_status <> 'PENDING' AND decided_by IS NOT NULL AND decided_at IS NOT NULL)),
    CONSTRAINT ck_stock_adjustments_snapshot CHECK (
        (adjustment_status = 'APPROVED' AND before_quantity IS NOT NULL AND after_quantity IS NOT NULL)
        OR (adjustment_status <> 'APPROVED' AND before_quantity IS NULL AND after_quantity IS NULL))
);

CREATE INDEX idx_stock_adjustments_status_created ON public.stock_adjustments (adjustment_status, created_at DESC);
CREATE INDEX idx_stock_adjustments_location ON public.stock_adjustments (location_id, created_at DESC);
CREATE INDEX idx_stock_adjustments_product ON public.stock_adjustments (product_id);

COMMENT ON TABLE  public.stock_adjustments                 IS 'Stock corrections (damage, loss, supplier return...). Never deleted. product_id, location_id and user ids come from other modules: no foreign keys';
COMMENT ON COLUMN public.stock_adjustments.quantity        IS 'Positive base units; direction says whether stock goes up or down';
COMMENT ON COLUMN public.stock_adjustments.before_quantity IS 'Stock before the approval; set only when APPROVED (FR-040)';
