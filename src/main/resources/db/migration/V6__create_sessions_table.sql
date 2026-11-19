-- Flyway migration V6: sessions table (server-side login sessions).
-- Matches the SessionJpaEntity class.
-- Never edit this file after it has been applied; add a new V7__... file for changes.

CREATE TABLE public.sessions (
    session_id   UUID          NOT NULL,
    user_id      UUID          NOT NULL,
    -- SHA-256 of the token, 64 hex characters. The token itself is never stored.
    token_hash   VARCHAR(64)   NOT NULL,
    client_type  VARCHAR(10)   NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL,
    -- fixed at login (created_at + 12 hours) and never extended
    expires_at   TIMESTAMPTZ   NOT NULL,
    ip_address   VARCHAR(45),
    user_agent   VARCHAR(255),
    CONSTRAINT pk_sessions PRIMARY KEY (session_id),
    -- one session per user: a new login must delete the old one first
    CONSTRAINT uq_sessions_user_id UNIQUE (user_id),
    -- also gives the index used to look a session up by its token
    CONSTRAINT uq_sessions_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_sessions_client_type CHECK (client_type IN ('WEB', 'MOBILE')),
    CONSTRAINT ck_sessions_expiry CHECK (expires_at > created_at),
    -- deleting a user ends their session
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES public.users (user_id) ON DELETE CASCADE
);

-- for the cleanup job that deletes expired sessions
CREATE INDEX idx_sessions_expires_at ON public.sessions (expires_at);

COMMENT ON TABLE public.sessions IS 'Login sessions. One per user. Fixed lifetime, no extension.';