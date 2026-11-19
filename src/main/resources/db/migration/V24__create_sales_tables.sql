-- =============================================================
-- V24 – Sales module
-- =============================================================
-- Orders (an order is not a sale until it is COMPLETED), their lines, an append-only history and receipts.
-- Creating an order never deducts stock or takes payment: only completing it does, in one transaction
-- (SALE movements through Inventory and the settlement through Payment).
-- Money is whole FCFA in BIGINT. No foreign keys to other modules: location, customer, product, unit, price
-- and user ids are plain UUIDs. Foreign keys inside the module are allowed.
-- Adds two permissions the Payment/Transfer batch did not have: SALE_VALIDATE and SALE_COMPLETE.
-- Merge order: Inventory (V20), Transfer (V21), Payment (V22), sale-complete permission (V23), then this one.
-- =============================================================

CREATE SEQUENCE public.order_number_seq   START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE public.receipt_number_seq START WITH 1 INCREMENT BY 1;

-- ---------- permissions ----------
INSERT INTO public.permissions (permission_id, permission_code, created_at)
SELECT gen_random_uuid(), v.code, NOW()
FROM (VALUES ('SALE_VALIDATE'), ('SALE_COMPLETE')) AS v(code)
WHERE NOT EXISTS (SELECT 1 FROM public.permissions p WHERE p.permission_code = v.code);

-- ADMIN, OWNER and MANAGER get both (validate is for Manager and Owner; completing is for everyone who sells)
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
JOIN public.permissions p ON p.permission_code IN ('SALE_VALIDATE', 'SALE_COMPLETE')
WHERE r.role_name IN ('ADMIN', 'OWNER', 'MANAGER')
ON CONFLICT DO NOTHING;

-- STORE_EMPLOYEE completes sales but never validates them
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
JOIN public.permissions p ON p.permission_code = 'SALE_COMPLETE'
WHERE r.role_name = 'STORE_EMPLOYEE'
ON CONFLICT DO NOTHING;

-- ---------- orders ----------
CREATE TABLE public.orders (
    order_id         UUID         NOT NULL,
    order_number     VARCHAR(20)  NOT NULL,
    location_id      UUID         NOT NULL,
    customer_id      UUID         NOT NULL,
    order_status     VARCHAR(30)  NOT NULL,
    subtotal         BIGINT       NOT NULL DEFAULT 0,
    discount_total   BIGINT       NOT NULL DEFAULT 0,
    total_amount     BIGINT       NOT NULL DEFAULT 0,
    amount_paid      BIGINT,
    amount_due       BIGINT,
    note             VARCHAR(500),
    created_by       UUID         NOT NULL,
    validated_by     UUID,
    completed_by     UUID,
    cancelled_by     UUID,
    rejection_reason VARCHAR(500),
    cancel_reason    VARCHAR(500),
    created_at       TIMESTAMPTZ  NOT NULL,
    submitted_at     TIMESTAMPTZ,
    validated_at     TIMESTAMPTZ,
    completed_at     TIMESTAMPTZ,
    updated_at       TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_orders PRIMARY KEY (order_id),
    CONSTRAINT uq_orders_order_number UNIQUE (order_number),
    CONSTRAINT ck_orders_amounts CHECK (subtotal >= 0 AND discount_total >= 0 AND total_amount >= 0),
    CONSTRAINT ck_orders_total CHECK (total_amount = subtotal - discount_total),
    CONSTRAINT ck_orders_status CHECK (order_status IN (
        'PENDING', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_orders_completed_amounts CHECK (
        order_status <> 'COMPLETED'
        OR (amount_paid IS NOT NULL AND amount_due IS NOT NULL AND amount_paid + amount_due = total_amount)),
    CONSTRAINT ck_orders_validator CHECK (validated_by IS NULL OR validated_by <> created_by)
);

CREATE INDEX idx_orders_location_status_created ON public.orders (location_id, order_status, created_at);
CREATE INDEX idx_orders_customer_id   ON public.orders (customer_id);
CREATE INDEX idx_orders_created_by    ON public.orders (created_by);
CREATE INDEX idx_orders_completed_at  ON public.orders (completed_at);

COMMENT ON TABLE  public.orders IS 'An order is a sale only when COMPLETED. location_id, customer_id and user ids belong to other modules: plain UUIDs, no foreign keys. order_id is also the saleId used by Payment.';
COMMENT ON COLUMN public.orders.total_amount IS 'Always computed from the lines, never accepted from the client.';

-- ---------- order_items ----------
CREATE TABLE public.order_items (
    order_item_id     UUID    NOT NULL,
    order_id          UUID    NOT NULL,
    product_id        UUID    NOT NULL,
    unit_id           UUID    NOT NULL,
    conversion_factor INT     NOT NULL,
    quantity          INT     NOT NULL,
    unit_price        BIGINT,
    price_id          UUID,
    discount_amount   BIGINT  NOT NULL DEFAULT 0,
    line_total        BIGINT  NOT NULL DEFAULT 0,

    CONSTRAINT pk_order_items PRIMARY KEY (order_item_id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES public.orders (order_id),
    CONSTRAINT uq_order_items_order_product_unit UNIQUE (order_id, product_id, unit_id),
    CONSTRAINT ck_order_items_factor CHECK (conversion_factor >= 1),
    CONSTRAINT ck_order_items_quantity CHECK (quantity >= 1),
    CONSTRAINT ck_order_items_price CHECK (unit_price IS NULL OR unit_price > 0),
    CONSTRAINT ck_order_items_discount CHECK (discount_amount >= 0),
    CONSTRAINT ck_order_items_total CHECK (line_total >= 0)
);

CREATE INDEX idx_order_items_order_id   ON public.order_items (order_id);
CREATE INDEX idx_order_items_product_id ON public.order_items (product_id);

COMMENT ON TABLE  public.order_items IS 'Lines of an order. product_id, unit_id and price_id belong to other modules: plain UUIDs, no foreign keys.';
COMMENT ON COLUMN public.order_items.conversion_factor IS 'Copied from the product unit when the line is added, so a later change never alters an old order. Base quantity = quantity x factor.';
COMMENT ON COLUMN public.order_items.unit_price IS 'From pricing. Null while the line cannot be priced. Frozen at validation.';

-- ---------- order_events (append-only) ----------
CREATE TABLE public.order_events (
    event_id     UUID         NOT NULL,
    order_id     UUID         NOT NULL,
    event_type   VARCHAR(30)  NOT NULL,
    from_status  VARCHAR(30),
    to_status    VARCHAR(30)  NOT NULL,
    performed_by UUID         NOT NULL,
    occurred_at  TIMESTAMPTZ  NOT NULL,
    note         VARCHAR(500),

    CONSTRAINT pk_order_events PRIMARY KEY (event_id),
    CONSTRAINT fk_order_events_order FOREIGN KEY (order_id) REFERENCES public.orders (order_id),
    CONSTRAINT ck_order_events_type CHECK (event_type IN (
        'CREATED', 'UPDATED', 'CANCELLED', 'COMPLETED'))
);

CREATE INDEX idx_order_events_order_time ON public.order_events (order_id, occurred_at);

CREATE FUNCTION public.order_events_append_only() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'order_events is append-only: % is not allowed', TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_order_events_append_only
    BEFORE UPDATE OR DELETE ON public.order_events
    FOR EACH ROW EXECUTE FUNCTION public.order_events_append_only();

COMMENT ON TABLE public.order_events IS 'Every step of an order. APPEND-ONLY: trigger trg_order_events_append_only refuses UPDATE and DELETE. performed_by is a user id of another module.';

-- ---------- receipts ----------
CREATE TABLE public.receipts (
    receipt_id     UUID         NOT NULL,
    receipt_number VARCHAR(20)  NOT NULL,
    order_id       UUID         NOT NULL,
    issued_at      TIMESTAMPTZ  NOT NULL,
    issued_by      UUID         NOT NULL,

    CONSTRAINT pk_receipts PRIMARY KEY (receipt_id),
    CONSTRAINT fk_receipts_order FOREIGN KEY (order_id) REFERENCES public.orders (order_id),
    CONSTRAINT uq_receipts_receipt_number UNIQUE (receipt_number),
    CONSTRAINT uq_receipts_order_id UNIQUE (order_id)
);

COMMENT ON TABLE public.receipts IS 'One receipt per completed order. The printable content is built from the frozen order. issued_by is a user id of another module.';
