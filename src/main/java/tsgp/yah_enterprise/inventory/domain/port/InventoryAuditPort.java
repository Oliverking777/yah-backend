package tsgp.yah_enterprise.inventory.domain.port;

import java.util.List;
import java.util.UUID;

/**
 * Inventory's way to write to the audit log. Implemented in infrastructure/integration. The actor is the authenticated
 * user unless actorId is given. NOOP is for tests that do not care about auditing.
 */
public interface InventoryAuditPort {

    InventoryAuditPort NOOP = record -> { };

    void record(AuditRecord record);

    record AuditRecord(UUID actorId, String action, String entityType, UUID entityId, UUID locationId,
                       String reason, List<Change> changes) {
        public AuditRecord {
            changes = changes == null ? List.of() : List.copyOf(changes);
        }
    }

    record Change(String fieldName, String oldValue, String newValue) { }
}
