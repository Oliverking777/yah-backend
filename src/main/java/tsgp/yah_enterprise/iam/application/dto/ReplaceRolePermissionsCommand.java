package tsgp.yah_enterprise.iam.application.dto;

import java.util.Set;
import java.util.UUID;

/**
 * Input for the ReplaceRolePermissionsUseCase.
 * The set becomes the complete list of permissions of the role. An empty set removes them all.
 */
public record ReplaceRolePermissionsCommand(
        Set<UUID> permissionIds
) {}