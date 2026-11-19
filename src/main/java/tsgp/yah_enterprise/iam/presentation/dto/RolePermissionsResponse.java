package tsgp.yah_enterprise.iam.presentation.dto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.iam.application.dto.RolePermissionsResult;

/**
 * HTTP response body describing a role and the permissions it grants.
 */
@Schema(description = "A role and the permissions it grants")
public record RolePermissionsResponse(

        @Schema(description = "Id of the role", example = "3f2b8c1e-5a4d-4c7e-9b1a-2d6f8e0a1b3c")
        UUID roleId,

        @Schema(description = "Name of the role", example = "ADMIN")
        String roleName,

        @Schema(description = "Permissions granted by the role, ordered by code")
        List<PermissionResponse> permissions
) {
    public static RolePermissionsResponse from(RolePermissionsResult result) {
        return new RolePermissionsResponse(
                result.roleId(),
                result.roleName(),
                result.permissions().stream().map(PermissionResponse::from).toList());
    }
}