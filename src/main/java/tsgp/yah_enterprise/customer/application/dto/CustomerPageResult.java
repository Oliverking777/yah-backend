package tsgp.yah_enterprise.customer.application.dto;

import java.util.List;

public record CustomerPageResult(
        List<CustomerResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public CustomerPageResult {
        content = List.copyOf(content);
    }

    public static CustomerPageResult of(List<CustomerResult> content, int page, int size, long total) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new CustomerPageResult(content, page, size, total, totalPages,
                page + 1 < totalPages, page > 0);
    }
}
