package tsgp.yah_enterprise.location.presentation.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.location.application.dto.LocationPageResult;

/**
 * HTTP response body describing one page of locations.
 */
@Schema(description = "One page of locations")
public record LocationPageResponse(

        @Schema(description = "Locations on this page")
        List<LocationResponse> content,

        @Schema(description = "Zero-based page index", example = "0")
        int page,

        @Schema(description = "Page size", example = "20")
        int size,

        @Schema(description = "Total number of locations", example = "4")
        long totalElements,

        @Schema(description = "Total number of pages", example = "1")
        int totalPages,

        @Schema(description = "Whether a next page exists", example = "false")
        boolean hasNext,

        @Schema(description = "Whether a previous page exists", example = "false")
        boolean hasPrevious
) {
    public static LocationPageResponse from(LocationPageResult result) {
        return new LocationPageResponse(
                result.content().stream().map(LocationResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }
}