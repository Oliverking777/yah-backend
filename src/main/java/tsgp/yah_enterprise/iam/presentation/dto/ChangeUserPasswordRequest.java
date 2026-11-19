package tsgp.yah_enterprise.iam.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.iam.application.dto.ChangePasswordCommand;

/**
 * HTTP request body for setting another user's password (an administrator action).
 * Users change their own password with ChangeOwnPasswordRequest.
 */
@Schema(description = "The new password for a user")
public record ChangeUserPasswordRequest(

        @Schema(description = "The new password: at least 8 characters, at most 72 bytes, with a letter and a digit",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "New password is required")
        @Size(max = 200, message = "New password is too long")
        String newPassword
) {
    public ChangePasswordCommand toCommand() {
        return new ChangePasswordCommand(newPassword);
    }

    @Override
    public String toString() {
        return "ChangeUserPasswordRequest[newPassword=protected]";
    }
}