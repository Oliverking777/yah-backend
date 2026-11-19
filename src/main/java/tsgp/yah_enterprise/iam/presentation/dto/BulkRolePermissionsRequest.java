package tsgp.yah_enterprise.iam.presentation.dto;

import java.util.Set;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.iam.application.dto.BulkRolePermissionsCommand;

/**
 * HTTP request body for adding several permissions to a role, or removing several from it.
 */
@Schema(description = "Permissions to add to, or remove from, a role")
public record BulkRolePermissionsRequest(

        @Schema(description = "Ids of the permissions",
                example = "[\"7c9e6679-7425-40de-944b-e07fc1f90ae7\", \"1b4e28ba-2fa1-11d2-883f-0016d3cca427\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "permissionIds must contain at least one id")
        @Size(max = 500, message = "At most 500 permissions can be changed in one request")
        Set<@NotNull(message = "permissionIds must not contain null") UUID> permissionIds
) {
    public BulkRolePermissionsCommand toCommand() {
        return new BulkRolePermissionsCommand(permissionIds);
    }
}