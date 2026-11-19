-- Flyway migration V10: seeds the permission catalog, the built-in roles and their permissions.
-- Replaces the old startup bootstrap (BootstrapService): permissions and roles now come from this file.
-- Safe to run on a database where the bootstrap already ran: every insert skips rows that exist,
-- and an existing ADMIN role is upgraded to a system role with scope ALL.
-- Needs PostgreSQL 13+ (gen_random_uuid() is built in).
-- Never edit this file after it has been applied; add a new V11__... file for changes.
-- Every future module must add its own permissions in a new migration and grant them to ADMIN and OWNER.

-- ---------------------------------------------------------------
-- 1. Permissions (88 codes, one block per module)
-- ---------------------------------------------------------------
INSERT INTO public.permissions (permission_id, permission_code, created_at)
SELECT gen_random_uuid(), v.code, NOW()
FROM (VALUES
    -- 01 IAM
    ('PERMISSION_CREATE'), ('PERMISSION_READ'), ('PERMISSION_UPDATE'), ('PERMISSION_DELETE'),
    ('ROLE_CREATE'), ('ROLE_READ'), ('ROLE_UPDATE'), ('ROLE_DELETE'),
    ('ROLE_PERMISSION_READ'), ('ROLE_PERMISSION_UPDATE'),
    ('USER_CREATE'), ('USER_READ'), ('USER_UPDATE'), ('USER_DELETE'), ('USER_ACTIVATE'), ('USER_DEACTIVATE'),
    ('USER_ASSIGN_ROLE'), ('USER_PASSWORD_RESET'),
    -- 02 Location
    ('LOCATION_CREATE'), ('LOCATION_READ'), ('LOCATION_UPDATE'), ('LOCATION_ACTIVATE'), ('LOCATION_DEACTIVATE'),
    ('LOCATION_USER_READ'), ('LOCATION_USER_ASSIGN'), ('LOCATION_USER_REMOVE'),
    -- 03 Category
    ('CATEGORY_CREATE'), ('CATEGORY_READ'), ('CATEGORY_UPDATE'), ('CATEGORY_ACTIVATE'), ('CATEGORY_DEACTIVATE'),
    -- 04 Product
    ('PRODUCT_CREATE'), ('PRODUCT_READ'), ('PRODUCT_UPDATE'), ('PRODUCT_ACTIVATE'), ('PRODUCT_DEACTIVATE'),
    -- 05 Unit & Conversion
    ('UNIT_CREATE'), ('UNIT_READ'), ('UNIT_UPDATE'), ('UNIT_DEACTIVATE'),
    -- 06 Pricing
    ('PRICE_RULE_CREATE'), ('PRICE_RULE_READ'), ('PRICE_RULE_UPDATE'), ('PRICE_RULE_DEACTIVATE'),
    -- 07 Customer
    ('CUSTOMER_CREATE'), ('CUSTOMER_READ'), ('CUSTOMER_UPDATE'), ('CUSTOMER_DEACTIVATE'), ('CUSTOMER_HISTORY_READ'),
    -- 08 Supplier / Purchasing
    ('SUPPLIER_CREATE'), ('SUPPLIER_READ'), ('SUPPLIER_UPDATE'), ('SUPPLIER_DEACTIVATE'),
    ('PURCHASE_CREATE'), ('PURCHASE_READ'), ('PURCHASE_BACKDATE'),
    -- 09 Inventory
    ('STOCK_READ'), ('STOCK_READ_ALL'),
    ('STOCK_ADJUST_CREATE'), ('STOCK_ADJUST_READ'), ('STOCK_ADJUST_APPROVE'),
    ('STOCK_OVERSELL_OVERRIDE'),
    ('STOCK_THRESHOLD_READ'), ('STOCK_THRESHOLD_UPDATE'),
    -- 10 Transfer
    ('TRANSFER_CREATE'), ('TRANSFER_READ'), ('TRANSFER_READ_ALL'), ('TRANSFER_CONFIRM'),
    -- 11 Sales / Orders
    ('SALE_CREATE'), ('SALE_READ'), ('SALE_READ_ALL'), ('SALE_REVERSE'),
    -- 12 Payment / Credit
    ('PAYMENT_CREATE'), ('PAYMENT_READ'), ('BALANCE_READ'), ('CREDIT_APPROVE'),
    -- 13 Offline Sync
    ('SYNC_PUSH'), ('SYNC_PULL'), ('SYNC_STATUS_READ'), ('SYNC_CONFLICT_READ'), ('SYNC_CONFLICT_RESOLVE'),
    -- 14 Audit
    ('AUDIT_READ'),
    -- 15 Dashboard / Reports
    ('DASHBOARD_READ'), ('DASHBOARD_SUMMARY_READ'),
    ('REPORT_SALES_READ'), ('REPORT_STOCK_READ'), ('REPORT_DEBT_READ'), ('ALERT_READ')
) AS v(code)
ON CONFLICT (permission_code) DO NOTHING;

-- ---------------------------------------------------------------
-- 2. Roles
--    ADMIN, OWNER: system roles, work across every location.
--    MANAGER: custom role, works across every location.
--    WAREHOUSE_EMPLOYEE, STORE_EMPLOYEE: custom roles, must be assigned to one location.
-- ---------------------------------------------------------------
INSERT INTO public.roles (role_id, role_name, description, created_at, updated_at, location_scope, is_system)
VALUES
    (gen_random_uuid(), 'ADMIN',
     'Full access to the system. Holds every permission.',
     NOW(), NOW(), 'ALL', TRUE),
    (gen_random_uuid(), 'OWNER',
     'Business owner. Holds every permission and works across every location.',
     NOW(), NOW(), 'ALL', TRUE),
    (gen_random_uuid(), 'MANAGER',
     'Oversees daily operations across every location. Can approve credit and record stock adjustments. No access to roles and permissions.',
     NOW(), NOW(), 'ALL', FALSE),
    (gen_random_uuid(), 'WAREHOUSE_EMPLOYEE',
     'Records purchases and sends or receives stock transfers at the assigned warehouse.',
     NOW(), NOW(), 'ASSIGNED', FALSE),
    (gen_random_uuid(), 'STORE_EMPLOYEE',
     'Records sales, customer payments and stock transfers at the assigned store.',
     NOW(), NOW(), 'ASSIGNED', FALSE)
ON CONFLICT (role_name) DO UPDATE
    SET description    = EXCLUDED.description,
        location_scope = EXCLUDED.location_scope,
        is_system      = EXCLUDED.is_system,
        updated_at     = NOW()
    -- only the built-in roles are upgraded; a custom role that already exists is left untouched
    WHERE EXCLUDED.is_system;

-- ---------------------------------------------------------------
-- 3. Role permissions
-- ---------------------------------------------------------------

-- ADMIN and OWNER: every permission
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
CROSS JOIN public.permissions p
WHERE r.role_name IN ('ADMIN', 'OWNER')
ON CONFLICT DO NOTHING;

-- MANAGER: everything except managing permissions, roles and most user administration
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
CROSS JOIN public.permissions p
WHERE r.role_name = 'MANAGER'
  AND p.permission_code NOT LIKE 'PERMISSION\_%'
  AND p.permission_code NOT LIKE 'ROLE\_%'
  AND p.permission_code NOT IN ('USER_CREATE', 'USER_UPDATE', 'USER_DELETE', 'USER_ACTIVATE',
                                'USER_DEACTIVATE', 'USER_ASSIGN_ROLE', 'USER_PASSWORD_RESET')
ON CONFLICT DO NOTHING;

-- WAREHOUSE_EMPLOYEE: purchases and transfers of the assigned warehouse
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
JOIN public.permissions p ON p.permission_code IN (
    'CATEGORY_READ', 'LOCATION_READ', 'PRODUCT_READ', 'UNIT_READ', 'SUPPLIER_READ',
    'PURCHASE_CREATE', 'PURCHASE_READ',
    'STOCK_READ',
    'TRANSFER_CREATE', 'TRANSFER_READ', 'TRANSFER_CONFIRM',
    'SYNC_PUSH', 'SYNC_PULL', 'SYNC_STATUS_READ')
WHERE r.role_name = 'WAREHOUSE_EMPLOYEE'
ON CONFLICT DO NOTHING;

-- STORE_EMPLOYEE: sales, customer payments and transfers of the assigned store (no credit approval)
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
JOIN public.permissions p ON p.permission_code IN (
    'CATEGORY_READ', 'LOCATION_READ', 'PRODUCT_READ', 'UNIT_READ', 'PRICE_RULE_READ',
    'CUSTOMER_CREATE', 'CUSTOMER_READ', 'CUSTOMER_UPDATE', 'CUSTOMER_HISTORY_READ',
    'SALE_CREATE', 'SALE_READ',
    'PAYMENT_CREATE', 'PAYMENT_READ', 'BALANCE_READ',
    'STOCK_READ',
    'TRANSFER_CREATE', 'TRANSFER_READ', 'TRANSFER_CONFIRM',
    'SYNC_PUSH', 'SYNC_PULL', 'SYNC_STATUS_READ')
WHERE r.role_name = 'STORE_EMPLOYEE'
ON CONFLICT DO NOTHING;
