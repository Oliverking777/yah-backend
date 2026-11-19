-- Flyway migration V4: users table for the IAM module.
-- Matches the UserJpaEntity class.
-- Never edit this file after it has been applied; add a new V6__... file for changes.

CREATE TABLE public.users (
    user_id     UUID          NOT NULL,
    first_name  VARCHAR(100)  NOT NULL,
    last_name   VARCHAR(100)  NOT NULL,
    full_name   VARCHAR(201)  NOT NULL,
    username    VARCHAR(50)   NOT NULL,
    role_id     UUID          NOT NULL,
    active      BOOLEAN       NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    -- a role that still has users cannot be deleted
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES public.roles (role_id) ON DELETE RESTRICT
);

CREATE INDEX idx_users_role_id ON public.users (role_id);

COMMENT ON TABLE  public.users          IS 'Users of the system';
COMMENT ON COLUMN public.users.username IS 'Generated, lower case, unique, never changes';
COMMENT ON COLUMN public.users.full_name IS 'first_name + space + last_name, kept in sync by the domain';
