-- =============================================================
-- V29 – Self-confirmation allowed
-- =============================================================
-- Who may confirm a goods receipt or decide a stock adjustment is now decided only by the permissions
-- (RECEIPT_CONFIRM, STOCK_ADJUST_APPROVE): the user who recorded or requested it may confirm or decide it too.
-- The application no longer refuses it, so the database checks that did are removed.
-- Every other check (statuses, quantities, required fields) stays.
-- =============================================================

-- Goods receipts (V26): confirmed_by had to differ from received_by.
ALTER TABLE public.goods_receipts DROP CONSTRAINT IF EXISTS ck_goods_receipts_confirmer;

-- Stock adjustments (V20): drop any check that makes the decider differ from the requester,
-- whatever it was named. Checks that do not compare decided_by with created_by are kept.
DO $$
DECLARE
    c RECORD;
BEGIN
    IF to_regclass('public.stock_adjustments') IS NULL THEN
        RETURN;
    END IF;
    FOR c IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'public.stock_adjustments'::regclass
          AND contype = 'c'
          AND pg_get_constraintdef(oid) ~* 'decided_by\s*<>\s*created_by|created_by\s*<>\s*decided_by'
    LOOP
        EXECUTE format('ALTER TABLE public.stock_adjustments DROP CONSTRAINT %I', c.conname);
        RAISE NOTICE 'Dropped constraint % on stock_adjustments', c.conname;
    END LOOP;
END $$;
