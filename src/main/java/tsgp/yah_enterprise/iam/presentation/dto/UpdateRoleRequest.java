package tsgp.yah_enterprise.iam.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.iam.application.dto.UpdateRoleCommand;

/**
 * HTTP request body for updating a role.
 * A field that is left out (null) stays unchanged, but at least one field must be sent.
 */
@Schema(description = "Payload for updating a role. Fields left out are not changed.")
public record UpdateRoleRequest(

        @Schema(description = "New role name (stored in upper case)", example = "MANAGER",
                minLength = 3, maxLength = 50, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Pattern(
                regexp = "^[A-Za-z0-9_]{3,50}$",
                message = "Role name must be 3 to 50 characters: letters, digits and underscores only")
        String roleName,

        @Schema(description = "New description. Send an empty string to clear it.",
                example = "Can manage teams", maxLength = 255,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @Schema(description = "New location scope: ALL (works across every location) or ASSIGNED "
                + "(each user must belong to one location). Not allowed on system roles.",
                example = "ASSIGNED", allowableValues = {"ALL", "ASSIGNED"},
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String locationScope
) {

    @Schema(hidden = true)
    @AssertTrue(message = "At least one of roleName, description or locationScope must be provided")
    public boolean isAnyFieldProvided() {
        return roleName != null || description != null || locationScope != null;
    }

    public UpdateRoleCommand toCommand() {
        return new UpdateRoleCommand(roleName, description, locationScope);
    }
}