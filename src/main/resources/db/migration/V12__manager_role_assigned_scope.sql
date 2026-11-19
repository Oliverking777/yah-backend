-- Flyway migration V12: the MANAGER role is tied to one location.
-- Each store has its own manager, who validates the operations of that location only,
-- so MANAGER needs a location assignment (ASSIGNED) instead of working across every location (ALL).
-- Never edit this file after it has been applied; add a new V13__... file for changes.

UPDATE public.roles
   SET location_scope = 'ASSIGNED',
       description    = 'Manages one location: validates its operations, approves credit and records stock adjustments there. No access to roles and permissions.',
       updated_at     = NOW()
 WHERE role_name = 'MANAGER'
   AND is_system = FALSE;
