package tsgp.yah_enterprise.purchasing.application.dto;

import java.util.*;
import java.time.*;
import tsgp.yah_enterprise.purchasing.domain.model.*;

public record SupplierProductPageResult(List<SupplierProductResult> content, int page, int size, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
    public SupplierProductPageResult {
        content = List.copyOf(content);
    }

    public static SupplierProductPageResult of(List<SupplierProductResult> content, int page, int size, long total) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new SupplierProductPageResult(content, page, size, total, totalPages, page + 1 < totalPages, page > 0);
    }
}
