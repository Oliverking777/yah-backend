package tsgp.yah_enterprise.location.domain.model;

import tsgp.yah_enterprise.location.domain.exception.UserNotAssignableException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Links a user (employee/manager) to a location.
 * Only the user's id is stored; user data is obtained through UserDirectoryPort,
 * which keeps Location and IAM decoupled.
 * Rule: a user has at most one location assignment (enforced by the application
 * service via LocationAssignmentRepository.existsByUserId).
 */
public class LocationAssignment {

    private final UUID assignmentId;
    private final UUID locationId;
    private final UUID userId;
    private final Instant assignedAt;

    private LocationAssignment(UUID assignmentId, UUID locationId, UUID userId, Instant assignedAt) {
        this.assignmentId = Objects.requireNonNull(assignmentId, "assignmentId");
        this.locationId = Objects.requireNonNull(locationId, "locationId");
        this.userId = Objects.requireNonNull(userId, "userId");
        this.assignedAt = Objects.requireNonNull(assignedAt, "assignedAt");
    }

    /**
     * Assigns a user to a location.
     * The location must be active, the user must be active, and the user's role must need a location.
     */
    public static LocationAssignment assign(Location location, LocationUser user) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(user, "user");
        location.ensureActive();
        if (!user.active()) {
            throw new UserNotAssignableException(user.userId(), "user is inactive");
        }
        if (!user.locationRequired()) {
            throw new UserNotAssignableException(user.userId(),
                    "the user's role works across all locations and needs no assignment");
        }
        return new LocationAssignment(UUID.randomUUID(), location.getLocationId(), user.userId(), Instant.now());
    }

    public static LocationAssignment reconstitute(UUID assignmentId, UUID locationId,
                                                  UUID userId, Instant assignedAt) {
        return new LocationAssignment(assignmentId, locationId, userId, assignedAt);
    }

    public UUID getAssignmentId() { return assignmentId; }
    public UUID getLocationId() { return locationId; }
    public UUID getUserId() { return userId; }
    public Instant getAssignedAt() { return assignedAt; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof LocationAssignment other && assignmentId.equals(other.assignmentId));
    }

    @Override
    public int hashCode() {
        return assignmentId.hashCode();
    }
}