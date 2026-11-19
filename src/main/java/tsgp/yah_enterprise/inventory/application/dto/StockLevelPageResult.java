package tsgp.yah_enterprise.inventory.application.dto;

import java.util.List;

/** One page of StockLevel results. page is zero-based. */
public record StockLevelPageResult(
        List<StockLevelResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public StockLevelPageResult {
        content = List.copyOf(content);
    }

    public static StockLevelPageResult of(List<StockLevelResult> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new StockLevelPageResult(content, page, size, totalElements, totalPages, page + 1 < totalPages, page > 0);
    }
}
