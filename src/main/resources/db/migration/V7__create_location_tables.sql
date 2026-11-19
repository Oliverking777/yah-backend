-- Flyway migration V7: locations and location_assignments tables for the Location module.
-- Matches the LocationJpaEntity and LocationAssignmentJpaEntity classes.
-- Never edit this file after it has been applied; add a new V9__... file for changes.

CREATE TABLE public.locations (
    location_id     UUID         NOT NULL,
    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(30)  NOT NULL,
    location_type   VARCHAR(20)  NOT NULL,
    address         VARCHAR(500) NOT NULL,
    location_status VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_locations PRIMARY KEY (location_id),
    CONSTRAINT uq_locations_code UNIQUE (code),
    CONSTRAINT ck_locations_code_upper CHECK (code = UPPER(code)),
    CONSTRAINT ck_locations_type CHECK (location_type IN ('WAREHOUSE', 'STORE')),
    CONSTRAINT ck_locations_status CHECK (location_status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE public.location_assignments (
    assignment_id UUID        NOT NULL,
    location_id   UUID        NOT NULL,
    user_id       UUID        NOT NULL,
    assigned_at   TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_location_assignments PRIMARY KEY (assignment_id),
    -- a user belongs to at most one location
    CONSTRAINT uq_location_assignments_user UNIQUE (user_id),
    -- a location that still has assigned users cannot be deleted
    CONSTRAINT fk_location_assignments_location
        FOREIGN KEY (location_id) REFERENCES public.locations (location_id) ON DELETE RESTRICT
    -- user_id has no foreign key to the IAM users table on purpose: the modules stay decoupled
);

-- covers "which users work at this location?"
CREATE INDEX idx_location_assignments_location_id ON public.location_assignments (location_id);

COMMENT ON TABLE  public.locations                 IS 'Warehouses and stores of the Location module';
COMMENT ON COLUMN public.locations.code            IS 'Upper case: letters, digits, hyphens and underscores (2-30 chars); never changes';
COMMENT ON COLUMN public.locations.location_type   IS 'WAREHOUSE or STORE';
COMMENT ON COLUMN public.locations.location_status IS 'ACTIVE or INACTIVE';
COMMENT ON TABLE  public.location_assignments      IS 'Links a user to the one location they are assigned to';
COMMENT ON COLUMN public.location_assignments.user_id IS 'Id of a user of the IAM module (no foreign key)';
