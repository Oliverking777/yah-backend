package tsgp.yah_enterprise.inventory.application.dto;

import java.util.List;

/** One page of StockInitialization results. page is zero-based. */
public record StockInitializationPageResult(
        List<StockInitializationResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public StockInitializationPageResult {
        content = List.copyOf(content);
    }

    public static StockInitializationPageResult of(List<StockInitializationResult> content, int page, int size,
                                                    long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new StockInitializationPageResult(content, page, size, totalElements, totalPages,
                page + 1 < totalPages, page > 0);
    }
}
