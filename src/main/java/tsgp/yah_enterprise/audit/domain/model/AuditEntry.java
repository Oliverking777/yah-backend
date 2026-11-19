package tsgp.yah_enterprise.audit.domain.model;

import tsgp.yah_enterprise.audit.domain.exception.InvalidAuditEntryException;
import tsgp.yah_enterprise.audit.domain.valueobject.AuditAction;
import tsgp.yah_enterprise.audit.domain.valueobject.AuditActor;
import tsgp.yah_enterprise.audit.domain.valueobject.AuditChange;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * One line of the audit log: who did what, to which entity, where, when and with what outcome.
 * There is no update, no delete and no status: only create and reconstitute. The server builds every entry; a client
 * can never submit one. recordedAt is always the server's clock; occurredAt may come from a device (offline work) but
 * never later than recordedAt + 5 minutes.
 */
public class AuditEntry {

    public static final int MAX_CHANGES = 50;
    public static final int MAX_REASON_LENGTH = 500;
    public static final Duration CLOCK_TOLERANCE = Duration.ofMinutes(5);
    private static final Pattern ENTITY_TYPE = Pattern.compile("^[A-Z][A-Z0-9_]{1,39}$");

    private final UUID auditEntryId;
    private final AuditActor actor;
    private final AuditAction action;
    private final String entityType;
    private final UUID entityId;
    private final UUID locationId;
    private final AuditOutcome outcome;
    private final String reason;
    private final List<AuditChange> changes;
    private final UUID correlationId;
    private final Instant occurredAt;
    private final Instant recordedAt;

    private AuditEntry(UUID auditEntryId, AuditActor actor, AuditAction action, String entityType, UUID entityId,
                       UUID locationId, AuditOutcome outcome, String reason, List<AuditChange> changes,
                       UUID correlationId, Instant occurredAt, Instant recordedAt) {
        this.auditEntryId = Objects.requireNonNull(auditEntryId);
        this.actor = Objects.requireNonNull(actor);
        this.action = Objects.requireNonNull(action);
        this.entityType = entityType;
        this.entityId = entityId;
        this.locationId = locationId;
        this.outcome = Objects.requireNonNull(outcome);
        this.reason = reason;
        this.changes = List.copyOf(changes);
        this.correlationId = Objects.requireNonNull(correlationId);
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.recordedAt = Objects.requireNonNull(recordedAt);
    }

    /** A new entry. occurredAt may be null (then it is the recording time). */
    public static AuditEntry create(AuditActor actor, AuditAction action, String entityType, UUID entityId,
                                    UUID locationId, AuditOutcome outcome, String reason, List<AuditChange> changes,
                                    UUID correlationId, Instant occurredAt) {
        if (actor == null || action == null || outcome == null || correlationId == null) {
            throw new InvalidAuditEntryException("Actor, action, outcome and correlation id are required");
        }
        String type = entityType == null ? "" : entityType.trim();
        if (!ENTITY_TYPE.matcher(type).matches()) {
            throw new InvalidAuditEntryException("Entity type must be upper case letters, digits or _ (2 to 40 characters)");
        }
        String cleanedReason = reason == null ? "" : reason.trim();
        if (cleanedReason.length() > MAX_REASON_LENGTH) {
            throw new InvalidAuditEntryException("Reason must be at most " + MAX_REASON_LENGTH + " characters");
        }
        List<AuditChange> list = changes == null ? List.of() : changes;
        if (list.size() > MAX_CHANGES) {
            throw new InvalidAuditEntryException("An entry has at most " + MAX_CHANGES + " changed fields");
        }
        Instant recorded = Instant.now();
        Instant when = occurredAt == null || occurredAt.isAfter(recorded.plus(CLOCK_TOLERANCE)) ? recorded : occurredAt;
        return new AuditEntry(UUID.randomUUID(), actor, action, type, entityId, locationId, outcome,
                cleanedReason.isEmpty() ? null : cleanedReason, list, correlationId, when, recorded);
    }

    public static AuditEntry reconstitute(UUID auditEntryId, AuditActor actor, AuditAction action, String entityType,
                                          UUID entityId, UUID locationId, AuditOutcome outcome, String reason,
                                          List<AuditChange> changes, UUID correlationId, Instant occurredAt,
                                          Instant recordedAt) {
        return new AuditEntry(auditEntryId, actor, action, entityType, entityId, locationId, outcome, reason,
                changes == null ? List.of() : changes, correlationId, occurredAt, recordedAt);
    }

    public UUID getAuditEntryId() { return auditEntryId; }
    public AuditActor getActor() { return actor; }
    public AuditAction getAction() { return action; }
    public String getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public UUID getLocationId() { return locationId; }
    public AuditOutcome getOutcome() { return outcome; }
    public String getReason() { return reason; }
    public List<AuditChange> getChanges() { return changes; }
    public UUID getCorrelationId() { return correlationId; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getRecordedAt() { return recordedAt; }
}
