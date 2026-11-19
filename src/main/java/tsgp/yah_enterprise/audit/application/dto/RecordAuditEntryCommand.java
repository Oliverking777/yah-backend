package tsgp.yah_enterprise.audit.application.dto;

import tsgp.yah_enterprise.audit.domain.model.ActorType;
import tsgp.yah_enterprise.audit.domain.model.AuditOutcome;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Sent by other modules only (no endpoint). occurredAt may be a device time; it is clamped by the domain. */
public record RecordAuditEntryCommand(UUID actorId, ActorType actorType, String deviceId, String action,
                                      String entityType, UUID entityId, UUID locationId, AuditOutcome outcome,
                                      String reason, List<AuditChangeCommand> changes, UUID correlationId,
                                      Instant occurredAt) { }
