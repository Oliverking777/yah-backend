package tsgp.yah_enterprise.location.application.dto;

import tsgp.yah_enterprise.location.domain.model.LocationAssignment;
import tsgp.yah_enterprise.location.domain.model.LocationUser;

import java.time.Instant;
import java.util.UUID;

/**
 * Output describing a user assigned to a location.
 * User details come from IAM through UserDirectoryPort; the assignment date is Location's own.
 */
public record LocationUserResult(
        UUID userId,
        String fullName,
        String userName,
        String roleName,
        boolean active,
        Instant assignedAt
) {
    public static LocationUserResult from(LocationAssignment assignment, LocationUser user) {
        return new LocationUserResult(
                user.userId(),
                user.fullName(),
                user.userName(),
                user.roleName(),
                user.active(),
                assignment.getAssignedAt());
    }
}