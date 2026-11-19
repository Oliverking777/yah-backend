package tsgp.yah_enterprise.iam.presentation.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.iam.application.dto.AuthenticatedUser;

/**
 * HTTP response body describing who is logged in, what they may do, and when the session ends.
 */
@Schema(description = "The logged-in user and their permissions")
public record CurrentUserResponse(

        @Schema(description = "Id of the user", example = "5d1f9c2a-8b7e-4f3a-a6c1-0e9d2b4c7f18")
        UUID userId,

        @Schema(description = "Login name", example = "john.doe")
        String userName,

        @Schema(description = "Full name", example = "John Doe")
        String fullName,

        @Schema(description = "Id of the user's role")
        UUID roleId,

        @Schema(description = "Name of the user's role", example = "ADMIN")
        String roleName,

        @Schema(description = "Where the user works: ALL (every location) or ASSIGNED (must belong to one location)",
                example = "ASSIGNED", allowableValues = {"ALL", "ASSIGNED"})
        String locationScope,

        @Schema(description = "Permission codes granted by the role, sorted", example = "[\"ROLE_CREATE\", \"ROLE_READ\"]")
        List<String> permissions,

        @Schema(description = "When the current session ends (UTC)", example = "2026-09-30T22:15:30Z")
        Instant sessionExpiresAt
) {
    public static CurrentUserResponse from(AuthenticatedUser user) {
        return new CurrentUserResponse(
                user.userId(),
                user.userName(),
                user.fullName(),
                user.roleId(),
                user.roleName(),
                user.locationScope().toString(),
                user.permissions().stream().sorted().toList(),
                user.sessionExpiresAt());
    }
}