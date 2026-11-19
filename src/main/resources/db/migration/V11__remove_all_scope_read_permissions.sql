-- Flyway migration V11: removes the three "_ALL" read permissions.
-- Which records a user sees is now decided by the role's location_scope (ALL = every location,
-- ASSIGNED = only the user's own location), so these permissions no longer mean anything.
-- Never edit this file after it has been applied; add a new V12__... file for changes.

-- grants first: role_permissions.permission_id is ON DELETE RESTRICT
DELETE FROM public.role_permissions
 WHERE permission_id IN (
     SELECT permission_id
       FROM public.permissions
      WHERE permission_code IN ('STOCK_READ_ALL', 'TRANSFER_READ_ALL', 'SALE_READ_ALL'));

DELETE FROM public.permissions
 WHERE permission_code IN ('STOCK_READ_ALL', 'TRANSFER_READ_ALL', 'SALE_READ_ALL');
