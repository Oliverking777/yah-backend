package tsgp.yah_enterprise.iam.application.dto;

import java.time.Instant;
import java.util.UUID;

import tsgp.yah_enterprise.iam.domain.model.Role;

/**
 * Output describing a single role.
 */
public record RoleResult(
        UUID roleId,
        String roleName,
        String description,
        String locationScope,
        boolean system,
        Instant createdAt,
        Instant updatedAt
) {
    public static RoleResult from(Role role) {
        return new RoleResult(
                role.getRoleId(),
                role.getRoleName().value(),
                role.getDescription(),
                role.getLocationScope().toString(),
                role.isSystem(),
                role.getCreatedAt(),
                role.getUpdatedAt());
    }
}