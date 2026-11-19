package tsgp.yah_enterprise.transfer.presentation.dto;

import tsgp.yah_enterprise.transfer.application.dto.TransferResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TransferResponse(
        UUID transferId, String transferNumber,
        UUID sourceLocationId, String sourceLocationName,
        UUID destinationLocationId, String destinationLocationName,
        String transferStatus, boolean hasDiscrepancy, String note, List<TransferItemResponse> items,
        UUID createdBy, UUID receivedBy, UUID cancelledBy, String cancelReason,
        Instant createdAt, Instant receivedAt, Instant cancelledAt, Instant updatedAt) {

    public static TransferResponse from(TransferResult r) {
        return new TransferResponse(r.transferId(), r.transferNumber(),
                r.sourceLocationId(), r.sourceLocationName(), r.destinationLocationId(), r.destinationLocationName(),
                r.transferStatus(), r.hasDiscrepancy(), r.note(),
                r.items().stream().map(TransferItemResponse::from).toList(),
                r.createdBy(), r.receivedBy(), r.cancelledBy(), r.cancelReason(),
                r.createdAt(), r.receivedAt(), r.cancelledAt(), r.updatedAt());
    }
}
