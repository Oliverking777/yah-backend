package tsgp.yah_enterprise.product.application.dto;

import java.util.List;

public record ProductPageResult(
        List<ProductResult> content, int page, int size,
        long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {

    public ProductPageResult { content = List.copyOf(content); }

    public static ProductPageResult of(List<ProductResult> content, int page, int size, long total) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new ProductPageResult(content, page, size, total, totalPages,
                page + 1 < totalPages, page > 0);
    }
}
