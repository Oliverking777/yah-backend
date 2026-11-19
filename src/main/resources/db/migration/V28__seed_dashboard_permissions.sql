-- =============================================================
-- Dashboard module: no tables, only permissions.
-- DASHBOARD_VIEW: ADMIN, OWNER, MANAGER (a manager sees only the assigned location).
-- DASHBOARD_VIEW_ALL: ADMIN and OWNER (every location, including the warehouse).
-- =============================================================
INSERT INTO public.permissions (permission_id, permission_code, created_at)
SELECT gen_random_uuid(), c, NOW()
FROM (VALUES ('DASHBOARD_VIEW'), ('DASHBOARD_VIEW_ALL')) AS v(c)
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
JOIN public.permissions p ON p.permission_code = 'DASHBOARD_VIEW'
WHERE r.role_name IN ('ADMIN', 'OWNER', 'MANAGER')
ON CONFLICT DO NOTHING;

INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
JOIN public.permissions p ON p.permission_code = 'DASHBOARD_VIEW_ALL'
WHERE r.role_name IN ('ADMIN', 'OWNER')
ON CONFLICT DO NOTHING;
