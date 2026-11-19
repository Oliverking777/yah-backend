package tsgp.yah_enterprise.location.presentation.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import tsgp.yah_enterprise.location.application.dto.AssignUserToLocationCommand;

/**
 * HTTP request body for assigning a user to a location.
 */
@Schema(description = "The user to assign to the location")
public record AssignUserToLocationRequest(

        @Schema(description = "Id of the user", example = "5d1f9c2a-8b7e-4f3a-a6c1-0e9d2b4c7f18",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "userId is required")
        UUID userId
) {
    public AssignUserToLocationCommand toCommand() {
        return new AssignUserToLocationCommand(userId);
    }
}