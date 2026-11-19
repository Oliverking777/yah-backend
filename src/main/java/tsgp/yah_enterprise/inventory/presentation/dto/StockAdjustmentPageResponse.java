package tsgp.yah_enterprise.inventory.presentation.dto;

import tsgp.yah_enterprise.inventory.application.dto.StockAdjustmentPageResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "One page of StockAdjustment results")
public record StockAdjustmentPageResponse(
        @Schema(description = "Items on this page") List<StockAdjustmentResponse> content,
        @Schema(description = "Zero-based page index", example = "0") int page,
        @Schema(description = "Page size", example = "20") int size,
        @Schema(description = "Total number of items", example = "12") long totalElements,
        @Schema(description = "Total number of pages", example = "1") int totalPages,
        @Schema(description = "Whether a next page exists", example = "false") boolean hasNext,
        @Schema(description = "Whether a previous page exists", example = "false") boolean hasPrevious
) {
    public static StockAdjustmentPageResponse from(StockAdjustmentPageResult r) {
        return new StockAdjustmentPageResponse(r.content().stream().map(StockAdjustmentResponse::from).toList(), r.page(), r.size(),
                r.totalElements(), r.totalPages(), r.hasNext(), r.hasPrevious());
    }
}
