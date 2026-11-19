package tsgp.yah_enterprise.iam.presentation.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.iam.application.dto.CreateUserCommand;

/**
 * HTTP request body for creating a user. The login name is not sent: it is generated from the names.
 */
@Schema(description = "Data needed to create a user")
public record CreateUserRequest(

        @Schema(description = "First name", example = "John", maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name is too long")
        String firstName,

        @Schema(description = "Last name", example = "Doe", maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name is too long")
        String lastName,

        @Schema(description = "Id of the user's role", example = "3f2b8c1e-5a4d-4c7e-9b1a-2d6f8e0a1b3c",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "roleId is required")
        UUID roleId,

        @Schema(description = "Initial password: at least 8 characters, at most 72 bytes, with a letter and a digit",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        @Size(max = 200, message = "Password is too long")
        String password
) {
    public CreateUserCommand toCommand() {
        return new CreateUserCommand(firstName, lastName, roleId, password);
    }

    @Override
    public String toString() {
        return "CreateUserRequest[firstName=" + firstName + ", lastName=" + lastName
                + ", roleId=" + roleId + ", password=protected]";
    }
}