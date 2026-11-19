-- =============================================================
-- V30 – Stock Initialization
-- =============================================================
-- Allows the Owner to record the initial quantities of multiple products at one location
-- in a single atomic operation. No approval step: the initialization is COMPLETED on submission.
--
-- New tables:
--   stock_initializations       – aggregate root (one per submission)
--   stock_initialization_items  – one row per product line (child of the root)
--
-- The unique constraint uq_stock_init_items_location_product (location_id, product_id) is the
-- database-level guarantee that a product can be initialized at a given location only once.
-- location_id is denormalized onto the items table for this constraint.
--
-- The existing stock_movements table gains a new movement_type (INITIALIZATION, direction IN)
-- and a new reference_type (STOCK_INITIALIZATION) so the ledger can record initial-stock lines.
--
-- New permissions: STOCK_INIT_CREATE, STOCK_INIT_READ (granted to ADMIN and OWNER).
--
-- Never edit this file after it has been applied; add a new Vn__... file for changes.
-- =============================================================

-- ---------------------------------------------------------------
-- 1. Extend stock_movements constraints for INITIALIZATION
-- ---------------------------------------------------------------

ALTER TABLE public.stock_movements DROP CONSTRAINT IF EXISTS ck_stock_movements_type;
ALTER TABLE public.stock_movements ADD CONSTRAINT ck_stock_movements_type CHECK (movement_type IN
    ('PURCHASE', 'TRANSFER_IN', 'SALE_RETURN', 'TRANSFER_OUT', 'SALE', 'ADJUSTMENT', 'INITIALIZATION'));

ALTER TABLE public.stock_movements DROP CONSTRAINT IF EXISTS ck_stock_movements_reference_type;
ALTER TABLE public.stock_movements ADD CONSTRAINT ck_stock_movements_reference_type CHECK (reference_type IN
    ('PURCHASE', 'TRANSFER', 'SALE', 'ADJUSTMENT', 'STOCK_INITIALIZATION'));

ALTER TABLE public.stock_movements DROP CONSTRAINT IF EXISTS ck_stock_movements_sign;
ALTER TABLE public.stock_movements ADD CONSTRAINT ck_stock_movements_sign CHECK (
    (movement_type IN ('PURCHASE', 'TRANSFER_IN', 'SALE_RETURN', 'INITIALIZATION') AND quantity_delta > 0)
    OR (movement_type IN ('TRANSFER_OUT', 'SALE') AND quantity_delta < 0)
    OR movement_type = 'ADJUSTMENT');

ALTER TABLE public.stock_movements DROP CONSTRAINT IF EXISTS ck_stock_movements_reference_match;
ALTER TABLE public.stock_movements ADD CONSTRAINT ck_stock_movements_reference_match CHECK (
    (movement_type = 'PURCHASE' AND reference_type = 'PURCHASE')
    OR (movement_type IN ('TRANSFER_IN', 'TRANSFER_OUT') AND reference_type = 'TRANSFER')
    OR (movement_type IN ('SALE', 'SALE_RETURN') AND reference_type = 'SALE')
    OR (movement_type = 'ADJUSTMENT' AND reference_type = 'ADJUSTMENT')
    OR (movement_type = 'INITIALIZATION' AND reference_type = 'STOCK_INITIALIZATION'));

-- ---------------------------------------------------------------
-- 2. Permissions
-- ---------------------------------------------------------------

INSERT INTO public.permissions (permission_id, permission_code, created_at)
VALUES (gen_random_uuid(), 'STOCK_INIT_CREATE', NOW()),
       (gen_random_uuid(), 'STOCK_INIT_READ',  NOW())
ON CONFLICT (permission_code) DO NOTHING;

-- ADMIN and OWNER only: initializing stock is an owner-level action
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
JOIN public.permissions p ON p.permission_code IN ('STOCK_INIT_CREATE', 'STOCK_INIT_READ')
WHERE r.role_name IN ('ADMIN', 'OWNER')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------
-- 3. Sequence for human numbers (INIT-000001)
-- ---------------------------------------------------------------

CREATE SEQUENCE public.stock_initialization_number_seq START WITH 1 INCREMENT BY 1;

-- ---------------------------------------------------------------
-- 4. stock_initializations (aggregate root)
-- ---------------------------------------------------------------

CREATE TABLE public.stock_initializations (
    initialization_id     UUID         NOT NULL,
    initialization_number VARCHAR(20)  NOT NULL,
    location_id           UUID         NOT NULL,
    status                VARCHAR(20)  NOT NULL,
    initialized_by        UUID         NOT NULL,
    created_at            TIMESTAMPTZ  NOT NULL,
    completed_at          TIMESTAMPTZ,
    CONSTRAINT pk_stock_initializations PRIMARY KEY (initialization_id),
    CONSTRAINT uq_stock_initializations_number UNIQUE (initialization_number),
    CONSTRAINT ck_stock_initializations_status CHECK (status IN ('COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_stock_initializations_completed CHECK (
        (status = 'COMPLETED' AND completed_at IS NOT NULL)
        OR (status = 'CANCELLED' AND completed_at IS NULL))
);

CREATE INDEX idx_stock_initializations_status  ON public.stock_initializations (status, created_at DESC);
CREATE INDEX idx_stock_initializations_location ON public.stock_initializations (location_id, created_at DESC);

COMMENT ON TABLE  public.stock_initializations                       IS 'Bulk initial stock setup: one location, multiple products, submitted atomically. Never deleted. location_id and user ids come from other modules: no foreign keys';
COMMENT ON COLUMN public.stock_initializations.initialization_number IS 'Human number (INIT-000001) from stock_initialization_number_seq';
COMMENT ON COLUMN public.stock_initializations.status               IS 'COMPLETED on submission; CANCELLED is reserved for a future cancel feature';

-- ---------------------------------------------------------------
-- 5. stock_initialization_items (child: one row per product)
-- ---------------------------------------------------------------

CREATE TABLE public.stock_initialization_items (
    item_id            UUID     NOT NULL,
    initialization_id  UUID     NOT NULL,
    product_id         UUID     NOT NULL,
    location_id        UUID     NOT NULL,
    unit_id            UUID     NOT NULL,
    conversion_factor  INTEGER  NOT NULL,
    counted_quantity   BIGINT   NOT NULL,
    base_quantity      BIGINT   NOT NULL,
    CONSTRAINT pk_stock_initialization_items PRIMARY KEY (item_id),
    -- one product per initialization
    CONSTRAINT uq_stock_init_items_init_product UNIQUE (initialization_id, product_id),
    -- a product can be initialized at a location only once (the core integrity rule)
    CONSTRAINT uq_stock_init_items_location_product UNIQUE (location_id, product_id),
    CONSTRAINT ck_stock_init_items_conversion CHECK (conversion_factor >= 1),
    CONSTRAINT ck_stock_init_items_counted   CHECK (counted_quantity >= 1),
    CONSTRAINT ck_stock_init_items_base      CHECK (base_quantity >= 1),
    CONSTRAINT ck_stock_init_items_base_eq   CHECK (base_quantity = counted_quantity * conversion_factor)
);

CREATE INDEX idx_stock_init_items_initialization ON public.stock_initialization_items (initialization_id);

COMMENT ON TABLE  public.stock_initialization_items                    IS 'Product lines of a stock initialization. location_id is denormalized from the parent for the unique constraint. product_id, location_id, unit_id come from other modules: no foreign keys';
COMMENT ON COLUMN public.stock_initialization_items.conversion_factor   IS 'Factor of the selected unit relative to the base unit (e.g. 24 pieces per carton). Denormalized at initialization time';
COMMENT ON COLUMN public.stock_initialization_items.counted_quantity     IS 'Physical count in the selected unit (e.g. 50 cartons)';
COMMENT ON COLUMN public.stock_initialization_items.base_quantity        IS 'counted_quantity * conversion_factor, in base units (posted to the ledger)';
