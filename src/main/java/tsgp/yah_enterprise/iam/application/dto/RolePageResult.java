package tsgp.yah_enterprise.iam.application.dto;

import java.util.List;

/**
 * Output describing one page of roles.
 * page is zero-based.
 */
public record RolePageResult(
        List<RoleResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public RolePageResult {
        content = List.copyOf(content);
    }

    public static RolePageResult of(List<RoleResult> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new RolePageResult(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page + 1 < totalPages,
                page > 0);
    }
}