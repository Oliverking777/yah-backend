-- =============================================================
-- V26 – Purchasing module
-- =============================================================
-- Purchases, their lines and discrepancies, goods receipts and their lines.
-- Foreign keys exist only INSIDE the module. supplier_id, destination_location_id, product_id, unit_id and the
-- user ids belong to other modules: plain UUIDs, no foreign keys.
-- Also adds the purchasing permissions. PURCHASE_CREATE and PURCHASE_READ (the "view" permission) already exist.
-- =============================================================

INSERT INTO public.permissions (permission_id, permission_code, created_at)
SELECT gen_random_uuid(), code, NOW()
FROM (VALUES ('PURCHASE_ORDER'), ('PURCHASE_CANCEL'), ('PURCHASE_CLOSE'), ('PURCHASE_DISCREPANCY_RESOLVE'),
             ('RECEIPT_RECORD'), ('RECEIPT_CONFIRM'), ('RECEIPT_VIEW')) AS v(code)
ON CONFLICT (permission_code) DO NOTHING;

-- ordering, cancelling, closing, resolving and confirming a receipt: ADMIN, OWNER, MANAGER
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
JOIN public.permissions p ON p.permission_code IN
     ('PURCHASE_ORDER', 'PURCHASE_CANCEL', 'PURCHASE_CLOSE', 'PURCHASE_DISCREPANCY_RESOLVE', 'RECEIPT_CONFIRM')
WHERE r.role_name IN ('ADMIN', 'OWNER', 'MANAGER')
ON CONFLICT DO NOTHING;

-- recording and viewing receipts: also the warehouse employee, who receives the goods
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
JOIN public.permissions p ON p.permission_code IN ('RECEIPT_RECORD', 'RECEIPT_VIEW')
WHERE r.role_name IN ('ADMIN', 'OWNER', 'MANAGER', 'WAREHOUSE_EMPLOYEE')
ON CONFLICT DO NOTHING;

CREATE SEQUENCE public.purchase_number_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE public.goods_receipt_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE public.purchases (
    purchase_id             UUID         NOT NULL,
    purchase_number         VARCHAR(20)  NOT NULL,
    supplier_id             UUID         NOT NULL,
    destination_location_id UUID         NOT NULL,
    purchase_status         VARCHAR(30)  NOT NULL,
    total_amount            BIGINT       NOT NULL,
    expected_delivery_date  DATE,
    note                    VARCHAR(500),
    created_by              UUID         NOT NULL,
    ordered_by              UUID,
    cancelled_by            UUID,
    closed_by               UUID,
    cancel_reason           VARCHAR(500),
    close_reason            VARCHAR(500),
    created_at              TIMESTAMPTZ  NOT NULL,
    ordered_at              TIMESTAMPTZ,
    cancelled_at            TIMESTAMPTZ,
    closed_at               TIMESTAMPTZ,
    updated_at              TIMESTAMPTZ  NOT NULL,
    version                 BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT pk_purchases PRIMARY KEY (purchase_id),
    CONSTRAINT uq_purchases_purchase_number UNIQUE (purchase_number),
    CONSTRAINT ck_purchases_status CHECK (purchase_status IN
        ('DRAFT', 'ORDERED', 'PARTIALLY_RECEIVED', 'RECEIVED', 'CLOSED', 'CANCELLED')),
    CONSTRAINT ck_purchases_total_amount CHECK (total_amount >= 0)
);

CREATE INDEX idx_purchases_supplier_created ON public.purchases (supplier_id, created_at);
CREATE INDEX idx_purchases_location_status  ON public.purchases (destination_location_id, purchase_status);
CREATE INDEX idx_purchases_status_ordered   ON public.purchases (purchase_status, ordered_at);

CREATE TABLE public.purchase_items (
    purchase_item_id    UUID    NOT NULL,
    purchase_id         UUID    NOT NULL,
    product_id          UUID    NOT NULL,
    unit_id             UUID    NOT NULL,
    conversion_factor   INT     NOT NULL,
    ordered_quantity    INT     NOT NULL,
    unit_purchase_price BIGINT  NOT NULL,
    line_total          BIGINT  NOT NULL,
    received_quantity   INT     NOT NULL DEFAULT 0,

    CONSTRAINT pk_purchase_items PRIMARY KEY (purchase_item_id),
    CONSTRAINT fk_purchase_items_purchase FOREIGN KEY (purchase_id) REFERENCES public.purchases (purchase_id),
    CONSTRAINT uq_purchase_items_purchase_product_unit UNIQUE (purchase_id, product_id, unit_id),
    CONSTRAINT ck_purchase_items_factor CHECK (conversion_factor >= 1),
    CONSTRAINT ck_purchase_items_ordered CHECK (ordered_quantity >= 1),
    CONSTRAINT ck_purchase_items_price CHECK (unit_purchase_price > 0),
    CONSTRAINT ck_purchase_items_line_total CHECK (line_total = ordered_quantity * unit_purchase_price),
    CONSTRAINT ck_purchase_items_received CHECK (received_quantity BETWEEN 0 AND ordered_quantity)
);

CREATE INDEX idx_purchase_items_product ON public.purchase_items (product_id);

CREATE TABLE public.goods_receipts (
    goods_receipt_id UUID         NOT NULL,
    receipt_number   VARCHAR(20)  NOT NULL,
    purchase_id      UUID         NOT NULL,
    delivery_note    VARCHAR(50),
    receipt_status   VARCHAR(20)  NOT NULL,
    received_by      UUID         NOT NULL,
    confirmed_by     UUID,
    cancelled_by     UUID,
    received_at      TIMESTAMPTZ  NOT NULL,
    confirmed_at     TIMESTAMPTZ,
    cancelled_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_goods_receipts PRIMARY KEY (goods_receipt_id),
    CONSTRAINT uq_goods_receipts_receipt_number UNIQUE (receipt_number),
    CONSTRAINT fk_goods_receipts_purchase FOREIGN KEY (purchase_id) REFERENCES public.purchases (purchase_id),
    CONSTRAINT ck_goods_receipts_status CHECK (receipt_status IN ('DRAFT', 'CONFIRMED', 'CANCELLED')),
    CONSTRAINT ck_goods_receipts_confirmer CHECK (confirmed_by IS NULL OR confirmed_by <> received_by)
);

CREATE INDEX idx_goods_receipts_purchase       ON public.goods_receipts (purchase_id);
CREATE INDEX idx_goods_receipts_status_created ON public.goods_receipts (receipt_status, created_at);

CREATE TABLE public.goods_receipt_items (
    goods_receipt_item_id UUID         NOT NULL,
    goods_receipt_id      UUID         NOT NULL,
    purchase_item_id      UUID         NOT NULL,
    quantity_delivered    INT          NOT NULL,
    quantity_accepted     INT          NOT NULL,
    quantity_rejected     INT          NOT NULL,
    rejection_type        VARCHAR(20),
    rejection_reason      VARCHAR(500),

    CONSTRAINT pk_goods_receipt_items PRIMARY KEY (goods_receipt_item_id),
    CONSTRAINT fk_goods_receipt_items_receipt FOREIGN KEY (goods_receipt_id) REFERENCES public.goods_receipts (goods_receipt_id),
    CONSTRAINT fk_goods_receipt_items_purchase_item FOREIGN KEY (purchase_item_id) REFERENCES public.purchase_items (purchase_item_id),
    CONSTRAINT uq_goods_receipt_items_receipt_purchase_item UNIQUE (goods_receipt_id, purchase_item_id),
    CONSTRAINT ck_goods_receipt_items_delivered CHECK (quantity_delivered >= 1),
    CONSTRAINT ck_goods_receipt_items_accepted CHECK (quantity_accepted >= 0),
    CONSTRAINT ck_goods_receipt_items_rejected CHECK (quantity_rejected >= 0),
    CONSTRAINT ck_goods_receipt_items_sum CHECK (quantity_accepted + quantity_rejected = quantity_delivered),
    CONSTRAINT ck_goods_receipt_items_rejection CHECK (quantity_rejected = 0
        OR (rejection_type IS NOT NULL AND rejection_reason IS NOT NULL))
);

CREATE TABLE public.purchase_discrepancies (
    discrepancy_id    UUID         NOT NULL,
    purchase_id       UUID         NOT NULL,
    purchase_item_id  UUID         NOT NULL,
    goods_receipt_id  UUID,
    discrepancy_type  VARCHAR(30)  NOT NULL,
    expected_quantity INT          NOT NULL,
    actual_quantity   INT          NOT NULL,
    reason            VARCHAR(500) NOT NULL,
    resolution_status VARCHAR(20)  NOT NULL,
    resolution_type   VARCHAR(20),
    resolution_note   VARCHAR(500),
    resolved_by       UUID,
    resolved_at       TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_purchase_discrepancies PRIMARY KEY (discrepancy_id),
    CONSTRAINT fk_purchase_discrepancies_purchase FOREIGN KEY (purchase_id) REFERENCES public.purchases (purchase_id),
    CONSTRAINT fk_purchase_discrepancies_item FOREIGN KEY (purchase_item_id) REFERENCES public.purchase_items (purchase_item_id),
    CONSTRAINT fk_purchase_discrepancies_receipt FOREIGN KEY (goods_receipt_id) REFERENCES public.goods_receipts (goods_receipt_id),
    CONSTRAINT ck_purchase_discrepancies_type CHECK (discrepancy_type IN
        ('REJECTED_GOODS', 'EXCESS_DELIVERY', 'SHORT_DELIVERY')),
    CONSTRAINT ck_purchase_discrepancies_status CHECK (resolution_status IN ('OPEN', 'RESOLVED')),
    CONSTRAINT ck_purchase_discrepancies_resolved CHECK (resolution_status = 'OPEN'
        OR (resolution_type IS NOT NULL AND resolution_note IS NOT NULL
            AND resolved_by IS NOT NULL AND resolved_at IS NOT NULL))
);

CREATE INDEX idx_purchase_discrepancies_purchase ON public.purchase_discrepancies (purchase_id);
CREATE INDEX idx_purchase_discrepancies_open ON public.purchase_discrepancies (resolution_status)
    WHERE resolution_status = 'OPEN';

COMMENT ON TABLE public.purchases IS 'Purchases from suppliers. supplier_id, destination_location_id and user ids belong to other modules: plain UUIDs, no foreign keys.';
COMMENT ON TABLE public.purchase_items IS 'Purchase lines. product_id and unit_id belong to other modules: plain UUIDs, no foreign keys. The price and conversion factor are snapshots.';
COMMENT ON TABLE public.goods_receipts IS 'Goods received against a purchase. Only a CONFIRMED receipt adds stock. User ids belong to the IAM module: no foreign keys.';
COMMENT ON TABLE public.goods_receipt_items IS 'Receipt lines: delivered = accepted + rejected.';
COMMENT ON TABLE public.purchase_discrepancies IS 'Differences between ordered and accepted quantities. Resolving one never changes stock. User ids: no foreign keys.';
