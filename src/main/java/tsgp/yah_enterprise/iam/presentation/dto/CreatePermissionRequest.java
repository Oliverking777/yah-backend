package tsgp.yah_enterprise.iam.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.iam.application.dto.CreatePermissionCommand;

/**
 * HTTP request body for creating a permission.
 */
@Schema(description = "Payload for creating a permission")
public record CreatePermissionRequest(

        @Schema(description = "Permission code (stored in upper case)", example = "ROLE_CREATE",
                minLength = 3, maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Permission code is required")
        @Size(min = 3, max = 100, message = "Permission code must be between 3 and 100 characters")
        @Pattern(
                regexp = "^[A-Za-z][A-Za-z0-9]*(_[A-Za-z0-9]+)*$",
                message = "Permission code may contain letters, digits and single underscores, and must start with a letter")
        String permissionCode
) {
    public CreatePermissionCommand toCommand() {
        return new CreatePermissionCommand(permissionCode);
    }
}