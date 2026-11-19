package tsgp.yah_enterprise.unit.presentation.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.unit.application.dto.UnitPageResult;

/** HTTP response body describing one page of units. */
@Schema(description = "One page of units")
public record UnitPageResponse(

        @Schema(description = "Units on this page")
        List<UnitResponse> content,

        @Schema(description = "Zero-based page index", example = "0")
        int page,

        @Schema(description = "Page size", example = "20")
        int size,

        @Schema(description = "Total number of units matching the filter", example = "3")
        long totalElements,

        @Schema(description = "Total number of pages", example = "1")
        int totalPages,

        @Schema(description = "Whether a next page exists", example = "false")
        boolean hasNext,

        @Schema(description = "Whether a previous page exists", example = "false")
        boolean hasPrevious
) {
    public static UnitPageResponse from(UnitPageResult result) {
        return new UnitPageResponse(
                result.content().stream().map(UnitResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }
}
