package tsgp.yah_enterprise.iam.presentation.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.iam.application.dto.PermissionPageResult;

/**
 * HTTP response body describing one page of permissions.
 */
@Schema(description = "One page of permissions")
public record PermissionPageResponse(

        @Schema(description = "Permissions on this page")
        List<PermissionResponse> content,

        @Schema(description = "Zero-based page index", example = "0")
        int page,

        @Schema(description = "Requested page size", example = "20")
        int size,

        @Schema(description = "Total number of permissions", example = "42")
        long totalElements,

        @Schema(description = "Total number of pages", example = "3")
        int totalPages,

        @Schema(description = "Whether a next page exists")
        boolean hasNext,

        @Schema(description = "Whether a previous page exists")
        boolean hasPrevious
) {
    public static PermissionPageResponse from(PermissionPageResult result) {
        return new PermissionPageResponse(
                result.content().stream().map(PermissionResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }
}