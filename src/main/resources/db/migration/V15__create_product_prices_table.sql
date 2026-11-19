-- Flyway migration V11: product_prices table for the Pricing module.
-- Matches the ProductPriceJpaEntity class.
-- Never edit this file after it has been applied; add a new V12__... file for changes.

-- Needed so an exclusion constraint can mix "=" on ids with "&&" on ranges.
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE public.product_prices (
    price_id         UUID         NOT NULL,
    product_id       UUID         NOT NULL,
    unit_id          UUID         NOT NULL,
    price            BIGINT       NOT NULL,
    minimum_quantity INT          NOT NULL,
    maximum_quantity INT,
    effective_from   TIMESTAMPTZ  NOT NULL,
    effective_to     TIMESTAMPTZ,
    price_status     VARCHAR(20)  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_product_prices PRIMARY KEY (price_id),
    CONSTRAINT ck_product_prices_price_positive CHECK (price > 0),
    CONSTRAINT ck_product_prices_min_quantity CHECK (minimum_quantity >= 1),
    CONSTRAINT ck_product_prices_max_quantity CHECK (maximum_quantity IS NULL OR maximum_quantity >= minimum_quantity),
    CONSTRAINT ck_product_prices_period CHECK (effective_to IS NULL OR effective_to > effective_from),
    CONSTRAINT ck_product_prices_status CHECK (price_status IN ('ACTIVE', 'INACTIVE'))
);

-- Looking up the prices of one unit of a product (overlap check and price resolver)
CREATE INDEX idx_product_prices_lookup ON public.product_prices (product_id, unit_id, price_status);

-- Listing the history of a product, newest first
CREATE INDEX idx_product_prices_history ON public.product_prices (product_id, effective_from DESC);

-- The database is the real guarantee that two ACTIVE prices of the same product and unit never overlap
-- in quantity range AND in period. A NULL upper bound means "no limit" for both ranges.
-- The period is [from, to): the end is exclusive, so a price may start exactly when the previous one ends.
ALTER TABLE public.product_prices
    ADD CONSTRAINT ex_product_prices_no_overlap
    EXCLUDE USING gist (
        product_id WITH =,
        unit_id WITH =,
        int4range(minimum_quantity, maximum_quantity, '[]') WITH &&,
        tstzrange(effective_from, effective_to, '[)') WITH &&
    ) WHERE (price_status = 'ACTIVE');

COMMENT ON TABLE  public.product_prices                  IS 'Prices of the Pricing module. product_id and unit_id come from the Product module: no foreign keys, modules share ids, not tables';
COMMENT ON COLUMN public.product_prices.price            IS 'Whole FCFA, greater than 0';
COMMENT ON COLUMN public.product_prices.maximum_quantity IS 'NULL means no upper bound';
COMMENT ON COLUMN public.product_prices.effective_to     IS 'Exclusive end. NULL means open ended';
COMMENT ON COLUMN public.product_prices.price_status     IS 'ACTIVE or INACTIVE. A price change closes the old row and creates a new one';