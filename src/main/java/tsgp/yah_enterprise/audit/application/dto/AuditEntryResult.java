package tsgp.yah_enterprise.audit.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuditEntryResult(UUID auditEntryId, UUID actorId, String actorType, String deviceId, String action,
                               String entityType, UUID entityId, UUID locationId, String outcome, String reason,
                               List<AuditChangeResult> changes, UUID correlationId, Instant occurredAt,
                               Instant recordedAt) {

    public AuditEntryResult {
        changes = List.copyOf(changes);
    }
}
