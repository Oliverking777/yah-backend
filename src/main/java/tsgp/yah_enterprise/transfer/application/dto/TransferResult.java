package tsgp.yah_enterprise.transfer.application.dto;

import tsgp.yah_enterprise.transfer.domain.model.Transfer;
import tsgp.yah_enterprise.transfer.domain.port.TransferLocation;
import tsgp.yah_enterprise.transfer.domain.port.TransferProduct;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * transferStatus is SUBMITTED, RECEIVED or CANCELLED. hasDiscrepancy is the receipt outcome (always false before
 * receipt). createdBy and createdAt are the sender and the time the goods were sent.
 */
public record TransferResult(
        UUID transferId, String transferNumber,
        UUID sourceLocationId, String sourceLocationName,
        UUID destinationLocationId, String destinationLocationName,
        String transferStatus, boolean hasDiscrepancy, String note, List<TransferItemResult> items,
        UUID createdBy, UUID receivedBy, UUID cancelledBy, String cancelReason,
        Instant createdAt, Instant receivedAt, Instant cancelledAt, Instant updatedAt) {

    public TransferResult {
        items = List.copyOf(items);
    }

    public static TransferResult of(Transfer t, Map<UUID, TransferProduct> products,
                                    Map<UUID, TransferLocation> locations) {
        TransferLocation source = locations.get(t.getSourceLocationId());
        TransferLocation destination = locations.get(t.getDestinationLocationId());
        List<TransferItemResult> items = t.getItems().stream()
                .map(i -> TransferItemResult.of(i, products.get(i.getProductId())))
                .toList();
        return new TransferResult(t.getTransferId(), t.getTransferNumber().value(),
                t.getSourceLocationId(), source == null ? null : source.locationName(),
                t.getDestinationLocationId(), destination == null ? null : destination.locationName(),
                t.getTransferStatus().name(), t.hasDiscrepancy(), t.getNote(), items,
                t.getCreatedBy(), t.getReceivedBy(), t.getCancelledBy(), t.getCancelReason(),
                t.getCreatedAt(), t.getReceivedAt(), t.getCancelledAt(), t.getUpdatedAt());
    }
}