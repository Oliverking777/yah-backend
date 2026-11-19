package tsgp.yah_enterprise.iam.presentation.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.iam.application.dto.UserResult;

/**
 * HTTP response body describing a user. Never contains any password data.
 */
@Schema(description = "A user")
public record UserResponse(

        @Schema(description = "Unique id of the user", example = "5d1f9c2a-8b7e-4f3a-a6c1-0e9d2b4c7f18")
        UUID userId,

        @Schema(description = "First name", example = "John")
        String firstName,

        @Schema(description = "Last name", example = "Doe")
        String lastName,

        @Schema(description = "First name and last name together", example = "John Doe")
        String fullName,

        @Schema(description = "Generated login name", example = "john.doe")
        String userName,

        @Schema(description = "Id of the user's role", example = "3f2b8c1e-5a4d-4c7e-9b1a-2d6f8e0a1b3c")
        UUID roleId,

        @Schema(description = "false if the account is deactivated and cannot log in")
        boolean active,

        @Schema(description = "When the user was created (UTC)", example = "2026-09-29T10:15:30Z")
        Instant createdAt,

        @Schema(description = "When the user was last changed (UTC)", example = "2026-09-29T10:15:30Z")
        Instant updatedAt,

        @Schema(description = "When the user last made a request or logged in (UTC). Can be a few minutes behind. "
                + "null if the user has never been active.",
                example = "2026-09-29T10:15:30Z", nullable = true)
        Instant lastActiveAt
) {
    public static UserResponse from(UserResult result) {
        return new UserResponse(
                result.userId(),
                result.firstName(),
                result.lastName(),
                result.fullName(),
                result.userName(),
                result.roleId(),
                result.active(),
                result.createdAt(),
                result.updatedAt(),
                result.lastActiveAt());
    }
}