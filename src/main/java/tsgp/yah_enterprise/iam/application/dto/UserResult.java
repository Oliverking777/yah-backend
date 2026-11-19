package tsgp.yah_enterprise.iam.application.dto;

import java.time.Instant;
import java.util.UUID;

import tsgp.yah_enterprise.iam.domain.model.User;

/**
 * Output describing a single user. Never contains any password data.
 */
public record UserResult(
        UUID userId,
        String firstName,
        String lastName,
        String fullName,
        String userName,
        UUID roleId,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        Instant lastActiveAt
) {
    public static UserResult from(User user) {
        return new UserResult(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getUserName().value(),
                user.getRoleId(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastActiveAt());
    }
}