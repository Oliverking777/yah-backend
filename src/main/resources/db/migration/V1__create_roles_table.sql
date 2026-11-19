-- Flyway migration V1: roles table for the IAM module.
-- Matches the RoleJpaEntity class.
-- Never edit this file after it has been applied; add a new V2__... file for changes.

CREATE TABLE public.roles (
    role_id     UUID         NOT NULL,
    role_name   VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (role_id),
    CONSTRAINT uq_roles_role_name UNIQUE (role_name)
);

COMMENT ON TABLE  public.roles           IS 'Roles of the IAM module';
COMMENT ON COLUMN public.roles.role_name IS 'Upper case: letters, digits and underscores (3-50 chars)';
