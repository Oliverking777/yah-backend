-- Flyway migration V8: categories table for the Category module.
-- Matches the CategoryJpaEntity class.
-- Never edit this file after it has been applied; add a new V9__... file for changes.

CREATE TABLE public.categories (
    category_id     UUID         NOT NULL,
    category_name   VARCHAR(100) NOT NULL,
    description     VARCHAR(255),
    category_status VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (category_id),
    CONSTRAINT ck_categories_name_not_blank CHECK (LENGTH(TRIM(category_name)) > 0),
    CONSTRAINT ck_categories_status CHECK (category_status IN ('ACTIVE', 'INACTIVE'))
);

-- names are unique ignoring case: "Spare Parts" and "spare parts" are the same name
CREATE UNIQUE INDEX uq_categories_category_name ON public.categories (LOWER(category_name));

COMMENT ON TABLE  public.categories                 IS 'Product categories of the Category module';
COMMENT ON COLUMN public.categories.category_name   IS 'Unique ignoring case (1-100 chars)';
COMMENT ON COLUMN public.categories.category_status IS 'ACTIVE or INACTIVE';
