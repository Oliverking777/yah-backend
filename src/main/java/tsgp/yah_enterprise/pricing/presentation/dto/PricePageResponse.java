package tsgp.yah_enterprise.pricing.presentation.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.pricing.application.dto.PricePageResult;

/**
 * HTTP response body describing one page of prices.
 */
@Schema(description = "One page of price rows")
public record PricePageResponse(

        @Schema(description = "Prices on this page")
        List<ProductPriceResponse> content,

        @Schema(description = "Zero-based page index", example = "0")
        int page,

        @Schema(description = "Page size", example = "20")
        int size,

        @Schema(description = "Total number of price rows", example = "12")
        long totalElements,

        @Schema(description = "Total number of pages", example = "1")
        int totalPages,

        @Schema(description = "Whether a next page exists", example = "false")
        boolean hasNext,

        @Schema(description = "Whether a previous page exists", example = "false")
        boolean hasPrevious
) {
    public static PricePageResponse from(PricePageResult result) {
        return new PricePageResponse(
                result.content().stream().map(ProductPriceResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious());
    }
}
