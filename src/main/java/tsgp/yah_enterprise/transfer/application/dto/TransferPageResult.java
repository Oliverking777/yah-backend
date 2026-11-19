package tsgp.yah_enterprise.transfer.application.dto;

import java.util.List;

/** One page of transfers. page is zero-based. */
public record TransferPageResult(List<TransferResult> content, int page, int size, long totalElements,
                                 int totalPages, boolean hasNext, boolean hasPrevious) {

    public TransferPageResult {
        content = List.copyOf(content);
    }

    public static TransferPageResult of(List<TransferResult> content, int page, int size, long total) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new TransferPageResult(content, page, size, total, totalPages, page + 1 < totalPages, page > 0);
    }
}
