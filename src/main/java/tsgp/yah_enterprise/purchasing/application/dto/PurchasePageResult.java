package tsgp.yah_enterprise.purchasing.application.dto;

import java.util.*;
import java.time.*;
import tsgp.yah_enterprise.purchasing.domain.model.*;

public record PurchasePageResult(List<PurchaseResult> content, int page, int size, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
    public PurchasePageResult {
        content = List.copyOf(content);
    }

    public static PurchasePageResult of(List<PurchaseResult> content, int page, int size, long total) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new PurchasePageResult(content, page, size, total, totalPages, page + 1 < totalPages, page > 0);
    }
}
