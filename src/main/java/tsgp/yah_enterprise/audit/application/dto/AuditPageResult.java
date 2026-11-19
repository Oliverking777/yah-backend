package tsgp.yah_enterprise.audit.application.dto;

import java.util.List;

public record AuditPageResult(List<AuditEntryResult> content, int page, int size, long totalElements,
                              int totalPages, boolean hasNext, boolean hasPrevious) {

    public AuditPageResult {
        content = List.copyOf(content);
    }

    public static AuditPageResult of(List<AuditEntryResult> content, int page, int size, long total) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new AuditPageResult(content, page, size, total, totalPages, page + 1 < totalPages, page > 0);
    }
}
