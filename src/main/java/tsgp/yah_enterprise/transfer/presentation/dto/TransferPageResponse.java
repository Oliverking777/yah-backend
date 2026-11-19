package tsgp.yah_enterprise.transfer.presentation.dto;

import tsgp.yah_enterprise.transfer.application.dto.TransferPageResult;

import java.util.List;

public record TransferPageResponse(List<TransferResponse> content, int page, int size, long totalElements,
                                   int totalPages, boolean hasNext, boolean hasPrevious) {

    public static TransferPageResponse from(TransferPageResult r) {
        return new TransferPageResponse(r.content().stream().map(TransferResponse::from).toList(),
                r.page(), r.size(), r.totalElements(), r.totalPages(), r.hasNext(), r.hasPrevious());
    }
}
