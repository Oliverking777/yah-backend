package tsgp.yah_enterprise.location.presentation.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.location.application.dto.LocationUserResult;

/**
 * HTTP response body describing a user assigned to a location.
 */
@Schema(description = "A user assigned to a location")
public record LocationUserResponse(

        @Schema(description = "Id of the user", example = "5d1f9c2a-8b7e-4f3a-a6c1-0e9d2b4c7f18")
        UUID userId,

        @Schema(description = "Full name", example = "John Doe")
        String fullName,

        @Schema(description = "Login name", example = "john.doe")
        String userName,

        @Schema(description = "Name of the user's role", example = "MANAGER")
        String roleName,

        @Schema(description = "Whether the user account is active", example = "true")
        boolean active,

        @Schema(description = "When the user was assigned to the location (UTC)", example = "2026-10-03T09:30:00Z")
        Instant assignedAt
) {
    public static LocationUserResponse from(LocationUserResult result) {
        return new LocationUserResponse(
                result.userId(),
                result.fullName(),
                result.userName(),
                result.roleName(),
                result.active(),
                result.assignedAt());
    }
}