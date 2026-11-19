package tsgp.yah_enterprise.pricing.application.dto;

import java.util.List;

/**
 * Output describing one page of prices.
 * page is zero-based.
 */
public record PricePageResult(
        List<ProductPriceResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public PricePageResult {
        content = List.copyOf(content);
    }

    public static PricePageResult of(List<ProductPriceResult> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PricePageResult(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page + 1 < totalPages,
                page > 0);
    }
}
