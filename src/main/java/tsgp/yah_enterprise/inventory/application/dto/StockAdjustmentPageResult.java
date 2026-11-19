package tsgp.yah_enterprise.inventory.application.dto;

import java.util.List;

/** One page of StockAdjustment results. page is zero-based. */
public record StockAdjustmentPageResult(
        List<StockAdjustmentResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public StockAdjustmentPageResult {
        content = List.copyOf(content);
    }

    public static StockAdjustmentPageResult of(List<StockAdjustmentResult> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new StockAdjustmentPageResult(content, page, size, totalElements, totalPages, page + 1 < totalPages, page > 0);
    }
}
