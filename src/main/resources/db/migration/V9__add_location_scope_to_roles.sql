-- Flyway migration V9: adds the location scope and the system flag to roles.
-- Matches the RoleJpaEntity class (location_scope, is_system).
-- Never edit this file after it has been applied; add a new V10__... file for changes.

ALTER TABLE public.roles
    -- ALL: users work across every location and need no assignment. ASSIGNED: each user belongs to one location.
    -- Existing roles become ASSIGNED, the safe default.
    ADD COLUMN location_scope VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED',
    -- built-in roles cannot be renamed, deleted, or given another scope
    ADD COLUMN is_system      BOOLEAN     NOT NULL DEFAULT FALSE;

ALTER TABLE public.roles
    ADD CONSTRAINT ck_roles_location_scope CHECK (location_scope IN ('ALL', 'ASSIGNED'));

-- The owner works across every location and is a built-in role.
-- Does nothing if no role named OWNER exists yet.
UPDATE public.roles
   SET location_scope = 'ALL',
       is_system      = TRUE
 WHERE role_name = 'OWNER';

COMMENT ON COLUMN public.roles.location_scope IS 'ALL (works across every location, no assignment) or ASSIGNED (must belong to one location)';
COMMENT ON COLUMN public.roles.is_system      IS 'true for built-in roles: cannot be renamed, deleted, or given another location scope';
