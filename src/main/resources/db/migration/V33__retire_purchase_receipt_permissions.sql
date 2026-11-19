-- Flyway migration V33: permissions after the purchase lifecycle became SUBMITTED, CONFIRMED, CANCELLED (V32).
-- A purchase is ordered when it is created, there are no goods receipts, no close and no discrepancy resolution,
-- so five permissions no longer guard anything: PURCHASE_ORDER, PURCHASE_CLOSE, PURCHASE_DISCREPANCY_RESOLVE,
-- RECEIPT_RECORD and RECEIPT_VIEW.
-- What remains for purchasing: PURCHASE_CREATE (order, change), PURCHASE_READ, PURCHASE_CANCEL and
-- RECEIPT_CONFIRM (confirm a delivery; with PURCHASE_CREATE also a direct purchase).
-- Every statement is safe to run whether or not an earlier migration created these codes.
-- Never edit this file after it has been applied; add a new V34__... file for changes.

-- 1. The two permissions still in use exist (no-op when an earlier migration already added them).
INSERT INTO public.permissions (permission_id, permission_code, created_at)
SELECT gen_random_uuid(), v.code, NOW()
FROM (VALUES ('PURCHASE_CANCEL'), ('RECEIPT_CONFIRM')) AS v(code)
ON CONFLICT (permission_code) DO NOTHING;

-- 2. ADMIN and OWNER hold every permission; MANAGER holds everything outside IAM (as in V10).
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
JOIN public.permissions p ON p.permission_code IN ('PURCHASE_CANCEL', 'RECEIPT_CONFIRM')
WHERE r.role_name IN ('ADMIN', 'OWNER', 'MANAGER')
ON CONFLICT DO NOTHING;

-- 3. Recording a delivery is now confirming it: whoever could record receipts may confirm deliveries.
--    The warehouse employees receive the goods of their warehouse, so they get it in any case.
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT rp.role_id, confirm.permission_id
FROM public.role_permissions rp
JOIN public.permissions recorded ON recorded.permission_id = rp.permission_id
                                AND recorded.permission_code = 'RECEIPT_RECORD'
CROSS JOIN public.permissions confirm
WHERE confirm.permission_code = 'RECEIPT_CONFIRM'
ON CONFLICT DO NOTHING;

INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
JOIN public.permissions p ON p.permission_code = 'RECEIPT_CONFIRM'
WHERE r.role_name = 'WAREHOUSE_EMPLOYEE'
ON CONFLICT DO NOTHING;

-- 4. Retire the five codes: grants first, role_permissions.permission_id is ON DELETE RESTRICT.
DELETE FROM public.role_permissions
 WHERE permission_id IN (
     SELECT permission_id
       FROM public.permissions
      WHERE permission_code IN ('PURCHASE_ORDER', 'PURCHASE_CLOSE', 'PURCHASE_DISCREPANCY_RESOLVE',
                                'RECEIPT_RECORD', 'RECEIPT_VIEW'));

DELETE FROM public.permissions
 WHERE permission_code IN ('PURCHASE_ORDER', 'PURCHASE_CLOSE', 'PURCHASE_DISCREPANCY_RESOLVE',
                           'RECEIPT_RECORD', 'RECEIPT_VIEW');
