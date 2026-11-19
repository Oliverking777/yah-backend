package tsgp.yah_enterprise.location.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Read-only snapshot of a user, owned by the Location module.
 * Filled by the UserDirectoryPort adapter from IAM data.
 * Roles are dynamic entities in IAM, so Location only keeps the role's display name
 * and makes no business decision based on it.
 * locationRequired comes from the role's location scope: false for users who work across all
 * locations (e.g. the owner), who therefore cannot be assigned to one.
 */
public record LocationUser(
        UUID userId,
        String fullName,
        String userName,
        String roleName,
        boolean active,
        boolean locationRequired) {

    public LocationUser {
        Objects.requireNonNull(userId, "userId");
    }
}