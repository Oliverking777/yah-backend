package tsgp.yah_enterprise.category.presentation.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.category.application.dto.CategoryPageResult;

/**
 * HTTP response body describing one page of categories.
 */
@Schema(description = "One page of categories")
public record CategoryPageResponse(

        @Schema(description = "Categories on this page")
        List<CategoryResponse> content,

        @Schema(description = "Zero-based page index", example = "0")
        int page,

        @Schema(description = "Page size", example = "20")
        int size,

        @Schema(description = "Total number of categories", example = "12")
        long totalElements,

        @Schema(description = "Total number of pages", example = "1")
        int totalPages,

        @Schema(description = "Whether a next page exists", example = "false")
        boolean hasNext,

        @Schema(description = "Whether a previous page exists", example = "false")
        boolean hasPrevious
) {
    public static CategoryPageResponse from(CategoryPageResult result) {
        return new CategoryPageResponse(
                result.content().stream().map(CategoryResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }
}