package tsgp.yah_enterprise.iam.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.iam.application.dto.CreateRoleCommand;

/**
 * HTTP request body for creating a role.
 */
@Schema(description = "Payload for creating a role")
public record CreateRoleRequest(

        @Schema(description = "Role name (stored in upper case)", example = "ADMIN",
                minLength = 3, maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Role name is required")
        @Pattern(
                regexp = "^[A-Za-z0-9_]{3,50}$",
                message = "Role name must be 3 to 50 characters: letters, digits and underscores only")
        String roleName,

        @Schema(description = "What this role is for", example = "Full access to the system",
                maxLength = 255, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @Schema(description = "Where users of this role work. ALL: across every location, no assignment needed "
                + "(e.g. an owner). ASSIGNED: each user must belong to one location. Defaults to ASSIGNED.",
                example = "ASSIGNED", allowableValues = {"ALL", "ASSIGNED"},
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String locationScope
) {
    public CreateRoleCommand toCommand() {
        return new CreateRoleCommand(roleName, description, locationScope);
    }
}