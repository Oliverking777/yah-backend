-- Flyway migration V2: permissions table for the IAM module.
-- Matches the PermissionJpaEntity class.
-- Never edit this file after it has been applied; add a new V3__... file for changes.

CREATE TABLE public.permissions (
    permission_id   UUID          NOT NULL,
    permission_code VARCHAR(100)  NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL,
    CONSTRAINT pk_permissions PRIMARY KEY (permission_id),
    CONSTRAINT uq_permissions_permission_code UNIQUE (permission_code)
);

COMMENT ON TABLE  public.permissions                 IS 'Permissions of the IAM module';
COMMENT ON COLUMN public.permissions.permission_code IS 'Upper case, e.g. ROLE_CREATE: letters, digits and single underscores (3-100 chars)';
