package tsgp.yah_enterprise.audit.domain.repository;

import tsgp.yah_enterprise.audit.domain.model.AuditOutcome;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Every field may be null. from is inclusive, to is exclusive. A null or empty locationIds means any location. */
public record AuditSearchCriteria(UUID actorId, String action, String entityType, UUID entityId,
                                  Set<UUID> locationIds, AuditOutcome outcome, Instant from, Instant to) { }
