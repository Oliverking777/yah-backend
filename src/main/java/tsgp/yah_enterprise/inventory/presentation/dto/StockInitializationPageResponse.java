package tsgp.yah_enterprise.inventory.presentation.dto;

import tsgp.yah_enterprise.inventory.application.dto.StockInitializationPageResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "One page of StockInitialization results")
public record StockInitializationPageResponse(
        @Schema(description = "Items on this page") List<StockInitializationResponse> content,
        @Schema(description = "Zero-based page index", example = "0") int page,
        @Schema(description = "Page size", example = "20") int size,
        @Schema(description = "Total number of items", example = "3") long totalElements,
        @Schema(description = "Total number of pages", example = "1") int totalPages,
        @Schema(description = "Whether a next page exists", example = "false") boolean hasNext,
        @Schema(description = "Whether a previous page exists", example = "false") boolean hasPrevious
) {
    public static StockInitializationPageResponse from(StockInitializationPageResult r) {
        return new StockInitializationPageResponse(
                r.content().stream().map(StockInitializationResponse::from).toList(),
                r.page(), r.size(), r.totalElements(), r.totalPages(), r.hasNext(), r.hasPrevious());
    }
}
