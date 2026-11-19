package tsgp.yah_enterprise.iam.presentation.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.iam.application.dto.RoleResult;

/**
 * HTTP response body describing a role.
 */
@Schema(description = "A role")
public record RoleResponse(

        @Schema(description = "Unique id of the role", example = "3f2b8c1e-5a4d-4c7e-9b1a-2d6f8e0a1b3c")
        UUID roleId,

        @Schema(description = "Role name", example = "ADMIN")
        String roleName,

        @Schema(description = "What this role is for", example = "Full access to the system", nullable = true)
        String description,

        @Schema(description = "Where users of this role work: ALL (every location, no assignment needed) "
                + "or ASSIGNED (each user must belong to one location)",
                example = "ASSIGNED", allowableValues = {"ALL", "ASSIGNED"})
        String locationScope,

        @Schema(description = "true for built-in roles, which cannot be renamed, deleted or given another scope",
                example = "false")
        boolean system,

        @Schema(description = "When the role was created (UTC)", example = "2026-09-29T10:15:30Z")
        Instant createdAt,

        @Schema(description = "When the role was last changed (UTC)", example = "2026-09-29T10:15:30Z")
        Instant updatedAt
) {
    public static RoleResponse from(RoleResult result) {
        return new RoleResponse(
                result.roleId(),
                result.roleName(),
                result.description(),
                result.locationScope(),
                result.system(),
                result.createdAt(),
                result.updatedAt());
    }
}