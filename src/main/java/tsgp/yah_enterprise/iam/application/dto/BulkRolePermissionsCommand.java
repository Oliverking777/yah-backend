package tsgp.yah_enterprise.iam.application.dto;

import java.util.Set;
import java.util.UUID;

/**
 * Input for adding several permissions to a role, or removing several from it.
 */
public record BulkRolePermissionsCommand(
        Set<UUID> permissionIds
) {}