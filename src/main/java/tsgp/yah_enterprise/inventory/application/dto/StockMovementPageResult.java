package tsgp.yah_enterprise.inventory.application.dto;

import java.util.List;

/** One page of StockMovement results. page is zero-based. */
public record StockMovementPageResult(
        List<StockMovementResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public StockMovementPageResult {
        content = List.copyOf(content);
    }

    public static StockMovementPageResult of(List<StockMovementResult> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new StockMovementPageResult(content, page, size, totalElements, totalPages, page + 1 < totalPages, page > 0);
    }
}
