package tsgp.yah_enterprise.supplier.application.dto;

import java.util.List;

public record SupplierPageResult(
        List<SupplierResult> content, int page, int size,
        long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {

    public SupplierPageResult { content = List.copyOf(content); }

    public static SupplierPageResult of(List<SupplierResult> content, int page, int size, long total) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new SupplierPageResult(content, page, size, total, totalPages,
                page + 1 < totalPages, page > 0);
    }
}
