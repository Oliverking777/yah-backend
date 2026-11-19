package tsgp.yah_enterprise.iam.application.dto;

import java.util.List;

/**
 * Output describing one page of permissions.
 * page is zero-based.
 */
public record PermissionPageResult(
        List<PermissionResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public PermissionPageResult {
        content = List.copyOf(content);
    }

    public static PermissionPageResult of(List<PermissionResult> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PermissionPageResult(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page + 1 < totalPages,
                page > 0);
    }
}