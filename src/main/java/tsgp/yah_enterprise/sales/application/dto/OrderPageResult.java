package tsgp.yah_enterprise.sales.application.dto;

import java.util.List;

public record OrderPageResult(List<OrderResult> content, int page, int size, long totalElements, int totalPages,
                              boolean hasNext, boolean hasPrevious) {

    public OrderPageResult {
        content = List.copyOf(content);
    }

    public static OrderPageResult of(List<OrderResult> content, int page, int size, long total) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new OrderPageResult(content, page, size, total, totalPages, page + 1 < totalPages, page > 0);
    }
}
