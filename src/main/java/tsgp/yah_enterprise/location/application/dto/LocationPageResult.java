package tsgp.yah_enterprise.location.application.dto;

import java.util.List;

/**
 * Output describing one page of locations.
 * page is zero-based.
 */
public record LocationPageResult(
        List<LocationResult> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public LocationPageResult {
        content = List.copyOf(content);
    }

    public static LocationPageResult of(List<LocationResult> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new LocationPageResult(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page + 1 < totalPages,
                page > 0);
    }
}