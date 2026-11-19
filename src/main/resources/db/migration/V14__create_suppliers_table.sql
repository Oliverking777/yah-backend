-- =============================================================
-- V13 – Supplier module
-- =============================================================
-- No foreign keys to other modules.
-- supplier_code is system-generated from supplier_code_seq (SUP-000001 format).
-- supplier_name is unique ignoring case.
-- =============================================================

CREATE SEQUENCE public.supplier_code_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE public.suppliers (
    supplier_id     UUID          NOT NULL,
    supplier_code   VARCHAR(20)   NOT NULL,
    supplier_name   VARCHAR(150)  NOT NULL,
    contact_person  VARCHAR(100),
    phone_number    VARCHAR(20),
    email           VARCHAR(150),
    address         VARCHAR(300),
    supplier_status VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL,

    CONSTRAINT pk_suppliers PRIMARY KEY (supplier_id),
    CONSTRAINT uq_suppliers_supplier_code UNIQUE (supplier_code),
    CONSTRAINT ck_suppliers_name_not_blank CHECK (TRIM(supplier_name) <> ''),
    CONSTRAINT ck_suppliers_status CHECK (supplier_status IN ('ACTIVE', 'INACTIVE'))
);

COMMENT ON TABLE  public.suppliers IS 'Identity and contact data for suppliers. Owns no purchases, stock or payables.';
COMMENT ON COLUMN public.suppliers.supplier_code IS 'System-generated immutable code in SUP-000001 format, from supplier_code_seq.';
COMMENT ON COLUMN public.suppliers.email IS 'Stored lower-case.';

CREATE UNIQUE INDEX uq_suppliers_supplier_name ON public.suppliers (LOWER(supplier_name));

CREATE INDEX idx_suppliers_phone_number ON public.suppliers (phone_number);
