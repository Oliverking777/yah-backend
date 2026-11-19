package tsgp.yah_enterprise.audit.application.dto;

import tsgp.yah_enterprise.audit.domain.model.AuditOutcome;

import java.time.Instant;
import java.util.UUID;

/** from is inclusive, to is exclusive. A range is required unless an entityId is given. Paging is clamped. */
public record AuditSearchRequest(UUID actorId, String action, String entityType, UUID entityId, UUID locationId,
                                 AuditOutcome outcome, Instant from, Instant to, int page, int size) {

    public AuditSearchRequest {
        page = PageLimits.page(page);
        size = PageLimits.size(size);
    }
}
