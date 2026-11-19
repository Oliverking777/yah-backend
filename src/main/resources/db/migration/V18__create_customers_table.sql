-- =============================================================
-- V18 – Customer module
-- =============================================================
-- No foreign keys to other modules. customer_code is system-generated
-- from customer_code_seq (CUS-000001 format).
-- walk_in: exactly one row may have walk_in = TRUE (partial unique index).
-- =============================================================

CREATE SEQUENCE public.customer_code_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE public.customers (
    customer_id     UUID          NOT NULL,
    customer_code   VARCHAR(20)   NOT NULL,
    customer_name   VARCHAR(150)  NOT NULL,
    phone_number    VARCHAR(20),
    email           VARCHAR(150),
    address         VARCHAR(300),
    customer_type   VARCHAR(20)   NOT NULL,
    customer_status VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    walk_in         BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL,

    CONSTRAINT pk_customers PRIMARY KEY (customer_id),
    CONSTRAINT uq_customers_customer_code UNIQUE (customer_code),
    CONSTRAINT ck_customers_name_not_blank CHECK (TRIM(customer_name) <> ''),
    CONSTRAINT ck_customers_type CHECK (customer_type IN ('INDIVIDUAL', 'BUSINESS')),
    CONSTRAINT ck_customers_status CHECK (customer_status IN ('ACTIVE', 'INACTIVE'))
);

COMMENT ON TABLE  public.customers IS 'Identity and contact data for customers. Owns no sales, payments or credit.';
COMMENT ON COLUMN public.customers.customer_code IS 'System-generated immutable code in CUS-000001 format, from customer_code_seq.';
COMMENT ON COLUMN public.customers.customer_type IS 'INDIVIDUAL or BUSINESS. Immutable after creation.';
COMMENT ON COLUMN public.customers.walk_in IS 'TRUE only for the single seeded anonymous walk-in customer. Protected from update and deactivation.';
COMMENT ON COLUMN public.customers.phone_number IS 'Normalised: digits and optional leading +, 8-15 digits. Not unique; duplicates produce a warning, not an error.';
COMMENT ON COLUMN public.customers.email IS 'Stored lower-case.';

-- Exactly one walk-in customer
CREATE UNIQUE INDEX uq_customers_one_walk_in ON public.customers (walk_in) WHERE walk_in;

-- Search indexes
CREATE INDEX idx_customers_phone_number ON public.customers (phone_number);
CREATE INDEX idx_customers_name_lower   ON public.customers (LOWER(customer_name));

-- Seed: the single walk-in customer used for anonymous sales
INSERT INTO public.customers (
    customer_id, customer_code, customer_name, customer_type, customer_status, walk_in, created_at, updated_at
) VALUES (
    gen_random_uuid(), 'WALK-IN', 'Walk-in Customer', 'INDIVIDUAL', 'ACTIVE', TRUE, NOW(), NOW()
);
