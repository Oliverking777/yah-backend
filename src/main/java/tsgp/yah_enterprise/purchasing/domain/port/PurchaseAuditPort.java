package tsgp.yah_enterprise.purchasing.domain.port;

import java.util.List;
import java.util.UUID;

/** Purchasing's way to write to the audit log. Implemented in infrastructure/integration. */
public interface PurchaseAuditPort {

    void record(AuditRecord record);

    record AuditRecord(UUID actorId, String action, String entityType, UUID entityId, UUID locationId,
                       String reason, List<Change> changes) {
        public AuditRecord {
            changes = changes == null ? List.of() : List.copyOf(changes);
        }
    }

    record Change(String fieldName, String oldValue, String newValue) { }
}
