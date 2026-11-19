package tsgp.yah_enterprise.iam.application.dto;

import java.time.Instant;
import java.util.UUID;

import tsgp.yah_enterprise.iam.domain.model.Permission;

/**
 * Output describing a single permission.
 */
public record PermissionResult(
        UUID permissionId,
        String permissionCode,
        Instant createdAt
) {
    public static PermissionResult from(Permission permission) {
        return new PermissionResult(
                permission.getPermissionId(),
                permission.getPermissionCode().value(),
                permission.getCreatedAt());
    }
}