-- =============================================================
-- V9 – Product module tables
-- =============================================================
-- products.category_id has NO foreign key: Category is a separate module.
-- product_units.unit_id has NO foreign key: Unit is a separate module.
-- product_units.product_id DOES have a FK: same module aggregate.
-- =============================================================

CREATE TABLE public.products (
                                 product_id     UUID          NOT NULL,
                                 product_code   VARCHAR(30)   NOT NULL,
                                 product_name   VARCHAR(150)  NOT NULL,
                                 description    VARCHAR(500),
                                 category_id    UUID          NOT NULL, -- no FK: cross-module reference
                                 product_status VARCHAR(20)   NOT NULL,
                                 created_at     TIMESTAMPTZ   NOT NULL,
                                 updated_at     TIMESTAMPTZ   NOT NULL,

                                 CONSTRAINT pk_products PRIMARY KEY (product_id),
                                 CONSTRAINT uq_products_product_code UNIQUE (product_code),
                                 CONSTRAINT ck_products_status CHECK (product_status IN ('ACTIVE', 'INACTIVE')),
                                 CONSTRAINT ck_products_name_not_blank CHECK (TRIM(product_name) <> '')
);

COMMENT ON TABLE  public.products IS 'Product catalogue entries (aggregate root).';
COMMENT ON COLUMN public.products.category_id IS 'Logical reference to categories.category_id; no FK because Category is a separate module.';
COMMENT ON COLUMN public.products.product_code IS 'Immutable, upper-cased business code. Regex ^[A-Z0-9][A-Z0-9_-]{1,29}$.';

CREATE INDEX idx_products_category_id  ON public.products (category_id);
CREATE INDEX idx_products_product_name ON public.products (LOWER(product_name));

-- =============================================================

CREATE TABLE public.product_units (
                                      product_unit_id   UUID    NOT NULL,
                                      product_id        UUID    NOT NULL,
                                      unit_id           UUID    NOT NULL, -- no FK: cross-module reference
                                      base_unit         BOOLEAN NOT NULL,
                                      conversion_factor INT     NOT NULL,
                                      unit_status       VARCHAR(20)  NOT NULL,
                                      created_at        TIMESTAMPTZ  NOT NULL,
                                      updated_at        TIMESTAMPTZ  NOT NULL,

                                      CONSTRAINT pk_product_units PRIMARY KEY (product_unit_id),
                                      CONSTRAINT fk_product_units_product FOREIGN KEY (product_id) REFERENCES public.products (product_id),
                                      CONSTRAINT uq_product_units_product_unit UNIQUE (product_id, unit_id),
                                      CONSTRAINT ck_product_units_status CHECK (unit_status IN ('ACTIVE', 'INACTIVE')),
                                      CONSTRAINT ck_product_units_factor
                                          CHECK ((base_unit AND conversion_factor = 1) OR (NOT base_unit AND conversion_factor >= 2))
);

COMMENT ON TABLE  public.product_units IS 'Units of measure assigned to a product (children of the Product aggregate).';
COMMENT ON COLUMN public.product_units.unit_id IS 'Logical reference to the Unit module; no FK because Unit is a separate module.';
COMMENT ON COLUMN public.product_units.base_unit IS 'True for the single base unit (factor = 1). Enforced by partial unique index below.';
COMMENT ON COLUMN public.product_units.conversion_factor IS '1 of this unit = N base units. Base unit = 1; non-base >= 2.';

-- Exactly one base unit per product
CREATE UNIQUE INDEX uq_product_units_one_base ON public.product_units (product_id) WHERE base_unit;