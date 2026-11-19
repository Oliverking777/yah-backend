-- Flyway migration V5: credentials table (one row per user, holds only the password hash).
-- Matches the CredentialJpaEntity class.
-- Never edit this file after it has been applied.

CREATE TABLE public.credentials (
    credential_id       UUID          NOT NULL,
    user_id             UUID          NOT NULL,
    password_hash       VARCHAR(255)  NOT NULL,
    created_at          TIMESTAMPTZ   NOT NULL,
    password_changed_at TIMESTAMPTZ   NOT NULL,
    CONSTRAINT pk_credentials PRIMARY KEY (credential_id),
    -- one credential per user
    CONSTRAINT uq_credentials_user_id UNIQUE (user_id),
    -- deleting a user removes the credential
    CONSTRAINT fk_credentials_user FOREIGN KEY (user_id) REFERENCES public.users (user_id) ON DELETE CASCADE
);

COMMENT ON TABLE public.credentials IS 'Login secrets. Never store or log plain-text passwords.';
