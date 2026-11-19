package tsgp.yah_enterprise.category.application.dto;

import java.util.List;

/**
 * Output describing one page of categories.
 * page is zero-based.
 */
public record CategoryPageResult(
        List<CategoryResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public CategoryPageResult {
        content = List.copyOf(content);
    }

    public static CategoryPageResult of(List<CategoryResult> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new CategoryPageResult(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page + 1 < totalPages,
                page > 0);
    }
}