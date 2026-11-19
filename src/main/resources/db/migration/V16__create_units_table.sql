-- Flyway migration V15: units table for the Unit module.
-- Matches the UnitJpaEntity class.
-- Never edit this file after it has been applied; add a new V16__... file for changes.

CREATE TABLE public.units (
    unit_id     UUID         NOT NULL,
    unit_name   VARCHAR(50)  NOT NULL,
    unit_code   VARCHAR(10)  NOT NULL,
    unit_status VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_units PRIMARY KEY (unit_id),
    CONSTRAINT uq_units_unit_code UNIQUE (unit_code),
    CONSTRAINT ck_units_name_not_blank CHECK (LENGTH(TRIM(unit_name)) > 0),
    CONSTRAINT ck_units_code_format CHECK (unit_code ~ '^[A-Z0-9]{1,10}$'),
    CONSTRAINT ck_units_status CHECK (unit_status IN ('ACTIVE', 'INACTIVE'))
);

-- names are unique ignoring case: "Carton" and "carton" are the same name
CREATE UNIQUE INDEX uq_units_unit_name ON public.units (LOWER(unit_name));

COMMENT ON TABLE  public.units             IS 'Global catalogue of measurement units (Piece, Dozen, Carton) of the Unit module';
COMMENT ON COLUMN public.units.unit_name   IS 'Unique ignoring case (1-50 chars)';
COMMENT ON COLUMN public.units.unit_code   IS 'Short upper-case code such as PCS, DZ, CTN. Unique and never changed';
COMMENT ON COLUMN public.units.unit_status IS 'ACTIVE or INACTIVE. Units are never deleted because products reference them';
