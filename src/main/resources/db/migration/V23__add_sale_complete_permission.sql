-- Flyway migration V23: adds the SALE_COMPLETE permission (a manager completes a pending order).
-- Completing an order is the moment stock is reduced, so it is kept separate from SALE_CREATE.
-- Granted to ADMIN, OWNER and MANAGER. Safe to run twice: every insert skips rows that exist.
-- MANAGER is granted explicitly because V10 only gave it the permissions that existed at that time.

INSERT INTO public.permissions (permission_id, permission_code, created_at)
VALUES (gen_random_uuid(), 'SALE_COMPLETE', NOW())
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
JOIN public.permissions p ON p.permission_code = 'SALE_COMPLETE'
WHERE r.role_name IN ('ADMIN', 'OWNER', 'MANAGER')
ON CONFLICT DO NOTHING;
