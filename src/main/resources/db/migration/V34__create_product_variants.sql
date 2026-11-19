-- Flyway migration V34: product variants. What is stocked and sold is now a variant of a product (the SKU), e.g.
-- Indian Rice "50 kg 10% broken", Flour "50 kg". A simple product has one variant without a name.
-- Units move from the product to the variant, because a 50 kg bag and a 25 kg bag convert differently.
--
-- Every existing product gets one unnamed variant whose id IS the product id, with the product code as its SKU, and
-- its units move to that variant unchanged. New products keep that rule for their first variant. So the tables of
-- other modules (stock, prices, purchase, sale, transfer and adjustment lines) still point at the right variant
-- through their product_id, and each module can rename that column to variant_id later without moving data.
-- Never edit this file after it has been applied; add a new V35__... file for changes.

-- ---------------------------------------------------------------
-- 1. Variants
-- ---------------------------------------------------------------
CREATE TABLE public.product_variants (
    variant_id     UUID          NOT NULL,
    product_id     UUID          NOT NULL,
    variant_name   VARCHAR(100),
    name_key       VARCHAR(100)  NOT NULL,
    sku            VARCHAR(30)   NOT NULL,
    barcode        VARCHAR(64),
    variant_status VARCHAR(20)   NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL,
    updated_at     TIMESTAMPTZ   NOT NULL,
    CONSTRAINT pk_product_variants PRIMARY KEY (variant_id),
    CONSTRAINT fk_product_variants_product FOREIGN KEY (product_id) REFERENCES public.products (product_id),
    -- names unique within the product ignoring case and spaces; '' is the unnamed variant, so at most one per product
    CONSTRAINT uq_product_variants_name UNIQUE (product_id, name_key),
    CONSTRAINT uq_product_variants_sku UNIQUE (sku),
    CONSTRAINT uq_product_variants_barcode UNIQUE (barcode),
    CONSTRAINT ck_product_variants_status CHECK (variant_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_product_variants_sku_format CHECK (sku ~ '^[A-Z0-9][A-Z0-9_-]{1,29}$'),
    CONSTRAINT ck_product_variants_barcode_format CHECK (barcode IS NULL OR barcode ~ '^[A-Za-z0-9]{4,64}$'),
    CONSTRAINT ck_product_variants_name CHECK (
        (variant_name IS NULL AND name_key = '') OR (TRIM(variant_name) <> '' AND name_key <> ''))
);

CREATE INDEX idx_product_variants_product ON public.product_variants (product_id);

COMMENT ON TABLE  public.product_variants              IS 'Stock keeping items of a product (children of the Product aggregate). Stock, prices and document lines refer to variant_id';
COMMENT ON COLUMN public.product_variants.variant_name IS 'e.g. "50 kg 10% broken". NULL for the unnamed variant of a simple product, which is then its only variant';
COMMENT ON COLUMN public.product_variants.name_key     IS 'variant_name in lower case without spaces, '''' when unnamed. Unique per product';
COMMENT ON COLUMN public.product_variants.sku          IS 'Unique, upper case, never changed. The first variant of a product defaults to the product code';
COMMENT ON COLUMN public.product_variants.barcode      IS 'Optional; unique when set';

INSERT INTO public.product_variants (variant_id, product_id, variant_name, name_key, sku, barcode, variant_status,
                                     created_at, updated_at)
SELECT p.product_id, p.product_id, NULL, '', p.product_code, NULL, 'ACTIVE', p.created_at, p.updated_at
FROM public.products p;

-- ---------------------------------------------------------------
-- 2. Units of a variant (replaces product_units)
-- ---------------------------------------------------------------
CREATE TABLE public.variant_units (
    variant_unit_id   UUID         NOT NULL,
    variant_id        UUID         NOT NULL,
    unit_id           UUID         NOT NULL, -- no FK: Unit is a separate module
    base_unit         BOOLEAN      NOT NULL,
    conversion_factor INT          NOT NULL,
    unit_status       VARCHAR(20)  NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_variant_units PRIMARY KEY (variant_unit_id),
    CONSTRAINT fk_variant_units_variant FOREIGN KEY (variant_id) REFERENCES public.product_variants (variant_id),
    CONSTRAINT uq_variant_units_variant_unit UNIQUE (variant_id, unit_id),
    CONSTRAINT ck_variant_units_status CHECK (unit_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_variant_units_factor
        CHECK ((base_unit AND conversion_factor = 1) OR (NOT base_unit AND conversion_factor >= 2))
);

-- exactly one base unit per variant
CREATE UNIQUE INDEX uq_variant_units_one_base ON public.variant_units (variant_id) WHERE base_unit;

COMMENT ON TABLE  public.variant_units                   IS 'Units a variant is counted in (children of the Product aggregate)';
COMMENT ON COLUMN public.variant_units.conversion_factor IS '1 of this unit = N base units of the variant. Base unit = 1; others >= 2';

-- same ids as before, so nothing that kept a product_unit_id breaks
INSERT INTO public.variant_units (variant_unit_id, variant_id, unit_id, base_unit, conversion_factor, unit_status,
                                  created_at, updated_at)
SELECT pu.product_unit_id, pu.product_id, pu.unit_id, pu.base_unit, pu.conversion_factor, pu.unit_status,
       pu.created_at, pu.updated_at
FROM public.product_units pu;

DROP TABLE public.product_units;
