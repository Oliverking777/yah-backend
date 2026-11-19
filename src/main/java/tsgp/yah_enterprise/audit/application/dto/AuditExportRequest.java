package tsgp.yah_enterprise.audit.application.dto;

import tsgp.yah_enterprise.audit.domain.model.AuditOutcome;

import java.time.Instant;
import java.util.UUID;

public record AuditExportRequest(UUID actorId, String action, String entityType, UUID entityId, UUID locationId,
                                 AuditOutcome outcome, Instant from, Instant to, AuditRequestContext context) { }
