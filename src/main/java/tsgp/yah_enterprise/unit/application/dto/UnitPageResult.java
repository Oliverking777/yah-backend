package tsgp.yah_enterprise.unit.application.dto;

import java.util.List;

/** Output describing one page of units. page is zero-based. */
public record UnitPageResult(
        List<UnitResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public UnitPageResult {
        content = List.copyOf(content);
    }

    public static UnitPageResult of(List<UnitResult> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new UnitPageResult(content, page, size, totalElements, totalPages, page + 1 < totalPages, page > 0);
    }
}
