package tsgp.yah_enterprise.iam.presentation.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.iam.application.dto.RolePageResult;

/**
 * HTTP response body describing one page of roles.
 */
@Schema(description = "One page of roles")
public record RolePageResponse(

        @Schema(description = "Roles on this page")
        List<RoleResponse> content,

        @Schema(description = "Zero-based page index", example = "0")
        int page,

        @Schema(description = "Requested page size", example = "20")
        int size,

        @Schema(description = "Total number of roles", example = "42")
        long totalElements,

        @Schema(description = "Total number of pages", example = "3")
        int totalPages,

        @Schema(description = "Whether a next page exists")
        boolean hasNext,

        @Schema(description = "Whether a previous page exists")
        boolean hasPrevious
) {
    public static RolePageResponse from(RolePageResult result) {
        return new RolePageResponse(
                result.content().stream().map(RoleResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }
}