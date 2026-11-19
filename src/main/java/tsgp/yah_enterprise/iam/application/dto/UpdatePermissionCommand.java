package tsgp.yah_enterprise.iam.application.dto;

/**
 * Input for the UpdatePermissionUseCase.
 * Plain data: the domain (PermissionCode / Permission) validates the value.
 */
public record UpdatePermissionCommand(
        String permissionCode
) {}