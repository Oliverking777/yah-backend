package tsgp.yah_enterprise.iam.presentation.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.iam.application.dto.PermissionResult;

/**
 * HTTP response body describing a permission.
 */
@Schema(description = "A permission")
public record PermissionResponse(

        @Schema(description = "Unique id of the permission", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
        UUID permissionId,

        @Schema(description = "Permission code", example = "ROLE_CREATE")
        String permissionCode,

        @Schema(description = "When the permission was created (UTC)", example = "2026-09-29T10:15:30Z")
        Instant createdAt
) {
    public static PermissionResponse from(PermissionResult result) {
        return new PermissionResponse(
                result.permissionId(),
                result.permissionCode(),
                result.createdAt());
    }
}