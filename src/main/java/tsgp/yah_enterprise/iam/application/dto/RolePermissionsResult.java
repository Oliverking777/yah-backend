package tsgp.yah_enterprise.iam.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Output describing a role and the permissions it grants (ordered by code).
 */
public record RolePermissionsResult(
        UUID roleId,
        String roleName,
        List<PermissionResult> permissions
) {
    public RolePermissionsResult {
        permissions = List.copyOf(permissions);
    }
}