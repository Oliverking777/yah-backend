package tsgp.yah_enterprise.iam.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.iam.application.dto.ChangeOwnPasswordCommand;

/**
 * HTTP request body for changing one's own password.
 */
@Schema(description = "Current and new password")
public record ChangeOwnPasswordRequest(

        @Schema(description = "The current password", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Current password is required")
        @Size(max = 200, message = "Current password is too long")
        String currentPassword,

        @Schema(description = "The new password: at least 8 characters, at most 72 bytes, with a letter and a digit",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "New password is required")
        @Size(max = 200, message = "New password is too long")
        String newPassword
) {
    public ChangeOwnPasswordCommand toCommand() {
        return new ChangeOwnPasswordCommand(currentPassword, newPassword);
    }

    @Override
    public String toString() {
        return "ChangeOwnPasswordRequest[currentPassword=protected, newPassword=protected]";
    }
}