package tsgp.yah_enterprise.iam.presentation.dto;

import java.util.Set;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.iam.application.dto.ReplaceRolePermissionsCommand;

/**
 * HTTP request body that sets the complete list of permissions of a role.
 */
@Schema(description = "The complete list of permissions the role should have")
public record ReplaceRolePermissionsRequest(

        @Schema(description = "Ids of the permissions. Send an empty array to remove all permissions.",
                example = "[\"7c9e6679-7425-40de-944b-e07fc1f90ae7\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "permissionIds is required (send an empty array to remove all permissions)")
        @Size(max = 500, message = "A role can have at most 500 permissions")
        Set<@NotNull(message = "permissionIds must not contain null") UUID> permissionIds
) {
    public ReplaceRolePermissionsCommand toCommand() {
        return new ReplaceRolePermissionsCommand(permissionIds);
    }
}