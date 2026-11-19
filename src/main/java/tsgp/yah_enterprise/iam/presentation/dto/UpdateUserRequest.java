package tsgp.yah_enterprise.iam.presentation.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.iam.application.dto.UpdateUserCommand;

/**
 * HTTP request body for updating a user.
 * A field that is left out (null) stays unchanged, but at least one field must be sent.
 * The login name cannot be changed, and the account is activated or deactivated with its own endpoints.
 */
@Schema(description = "Fields to change on a user. Fields left out are not changed.")
public record UpdateUserRequest(

        @Schema(description = "New first name", example = "John", maxLength = 100,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 100, message = "First name is too long")
        String firstName,

        @Schema(description = "New last name", example = "Doe", maxLength = 100,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 100, message = "Last name is too long")
        String lastName,

        @Schema(description = "Id of the new role. Needs the permission to assign roles.",
                example = "3f2b8c1e-5a4d-4c7e-9b1a-2d6f8e0a1b3c",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        UUID roleId
) {

    @Schema(hidden = true)
    @AssertTrue(message = "At least one of firstName, lastName or roleId must be provided")
    public boolean isAnyFieldProvided() {
        return firstName != null || lastName != null || roleId != null;
    }

    public UpdateUserCommand toCommand() {
        return new UpdateUserCommand(firstName, lastName, roleId);
    }
}