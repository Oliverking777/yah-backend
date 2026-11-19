package tsgp.yah_enterprise.iam.application.dto;

/**
 * Input for the CreatePermissionUseCase.
 * Plain data: the domain (PermissionCode / Permission) validates the value.
 */
public record CreatePermissionCommand(
        String permissionCode
) {}