-- Flyway migration V13: last activity time for users.
-- Matches the lastActiveAt field of UserJpaEntity.
-- Never edit this file after it has been applied; add a new V14__... file for changes.

-- Nullable: a user who has never been active has no value, and existing rows need no backfill.
ALTER TABLE public.users ADD COLUMN last_active_at TIMESTAMPTZ;

COMMENT ON COLUMN public.users.last_active_at IS 'Last authenticated request or login. Throttled, so it can be a few minutes behind. NULL = never active';
