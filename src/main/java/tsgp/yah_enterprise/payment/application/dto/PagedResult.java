package tsgp.yah_enterprise.payment.application.dto;

import java.util.List;

/** One page of results. page is zero-based. */
public record PagedResult<T>(List<T> content, int page, int size, long totalElements, int totalPages,
                             boolean hasNext, boolean hasPrevious) {

    public PagedResult {
        content = List.copyOf(content);
    }

    public static <T> PagedResult<T> of(List<T> content, int page, int size, long total) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new PagedResult<>(content, page, size, total, totalPages, page + 1 < totalPages, page > 0);
    }
}
