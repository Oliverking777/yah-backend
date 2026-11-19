package tsgp.yah_enterprise.iam.application.dto;

import java.util.List;

/**
 * Output describing one page of users.
 * page is zero-based.
 */
public record UserPageResult(
        List<UserResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public UserPageResult {
        content = List.copyOf(content);
    }

    public static UserPageResult of(List<UserResult> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new UserPageResult(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page + 1 < totalPages,
                page > 0);
    }
}