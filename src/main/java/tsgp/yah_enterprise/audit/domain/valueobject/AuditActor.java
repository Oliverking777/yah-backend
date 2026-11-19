package tsgp.yah_enterprise.audit.domain.valueobject;

import tsgp.yah_enterprise.audit.domain.exception.InvalidAuditEntryException;
import tsgp.yah_enterprise.audit.domain.model.ActorType;

import java.util.UUID;

/**
 * Who did it. actorId is null only for an unknown user (a failed login) or the system. deviceId is optional.
 * Stored as columns of the entry, not as a table.
 */
public record AuditActor(UUID actorId, ActorType actorType, String deviceId) {

    public static final int MAX_DEVICE_LENGTH = 100;

    public AuditActor {
        if (actorType == null) {
            throw new InvalidAuditEntryException("Actor type is required");
        }
        String cleaned = deviceId == null ? "" : deviceId.trim();
        if (cleaned.length() > MAX_DEVICE_LENGTH) {
            throw new InvalidAuditEntryException("Device id must be at most " + MAX_DEVICE_LENGTH + " characters");
        }
        deviceId = cleaned.isEmpty() ? null : cleaned;
    }
}
