-- =============================================================
-- V25 – Audit module
-- =============================================================
-- The audit log: who did what, to which entity, from which device, when, and with what outcome.
-- APPEND-ONLY at the database level: a trigger refuses UPDATE and DELETE (same mechanism as stock_movements,
-- transfer_events and order_events).
-- No foreign keys: actor, entity and location ids belong to other modules (plain UUIDs).
-- Adds the AUDIT_EXPORT permission (AUDIT_READ, which already exists, is the "view" permission).
-- Monthly partitioning and a retention period are open points.
-- =============================================================

INSERT INTO public.permissions (permission_id, permission_code, created_at)
VALUES (gen_random_uuid(), 'AUDIT_EXPORT', NOW())
ON CONFLICT (permission_code) DO NOTHING;

-- the export holds the whole log: ADMIN and OWNER only
INSERT INTO public.role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM public.roles r
JOIN public.permissions p ON p.permission_code = 'AUDIT_EXPORT'
WHERE r.role_name IN ('ADMIN', 'OWNER')
ON CONFLICT DO NOTHING;

CREATE TABLE public.audit_entries (
    audit_entry_id UUID          NOT NULL,
    actor_id       UUID,
    actor_type     VARCHAR(10)   NOT NULL,
    device_id      VARCHAR(100),
    action         VARCHAR(80)   NOT NULL,
    entity_type    VARCHAR(40)   NOT NULL,
    entity_id      UUID,
    location_id    UUID,
    outcome        VARCHAR(10)   NOT NULL,
    reason         VARCHAR(500),
    changes        JSONB,
    correlation_id UUID          NOT NULL,
    occurred_at    TIMESTAMPTZ   NOT NULL,
    recorded_at    TIMESTAMPTZ   NOT NULL,

    CONSTRAINT pk_audit_entries PRIMARY KEY (audit_entry_id),
    CONSTRAINT ck_audit_entries_actor_type CHECK (actor_type IN ('USER', 'SYSTEM')),
    CONSTRAINT ck_audit_entries_outcome CHECK (outcome IN ('SUCCESS', 'DENIED', 'FAILED')),
    CONSTRAINT ck_audit_entries_action CHECK (action ~ '^[A-Z][A-Z0-9_]*(\.[A-Z][A-Z0-9_]*)+$'),
    CONSTRAINT ck_audit_entries_entity_type CHECK (entity_type ~ '^[A-Z][A-Z0-9_]{1,39}$'),
    CONSTRAINT ck_audit_entries_changes CHECK (changes IS NULL OR jsonb_typeof(changes) = 'array')
);

CREATE INDEX idx_audit_entries_entity      ON public.audit_entries (entity_type, entity_id, occurred_at);
CREATE INDEX idx_audit_entries_actor       ON public.audit_entries (actor_id, occurred_at DESC);
CREATE INDEX idx_audit_entries_action      ON public.audit_entries (action, occurred_at DESC);
CREATE INDEX idx_audit_entries_location    ON public.audit_entries (location_id, occurred_at DESC);
CREATE INDEX idx_audit_entries_correlation ON public.audit_entries (correlation_id);
CREATE INDEX idx_audit_entries_occurred    ON public.audit_entries (occurred_at DESC);

CREATE FUNCTION public.audit_entries_append_only() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_entries is append-only: % is not allowed', TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_entries_append_only
    BEFORE UPDATE OR DELETE ON public.audit_entries
    FOR EACH ROW EXECUTE FUNCTION public.audit_entries_append_only();

COMMENT ON TABLE  public.audit_entries IS 'The audit log. APPEND-ONLY: trigger trg_audit_entries_append_only refuses UPDATE and DELETE. actor_id, entity_id and location_id belong to other modules: plain UUIDs, no foreign keys. Never store secrets.';
COMMENT ON COLUMN public.audit_entries.occurred_at IS 'When it happened. A device time is accepted only when not later than recorded_at + 5 minutes.';
COMMENT ON COLUMN public.audit_entries.recorded_at IS 'Always the server clock.';
COMMENT ON COLUMN public.audit_entries.changes IS 'JSON array of {fieldName, oldValue, newValue}. Field level, never a full snapshot. Sensitive values are masked as ***.';
