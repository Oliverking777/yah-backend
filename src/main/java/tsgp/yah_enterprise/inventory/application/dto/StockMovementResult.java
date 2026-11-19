package tsgp.yah_enterprise.inventory.application.dto;

import tsgp.yah_enterprise.inventory.domain.model.StockMovement;

import java.time.Instant;
import java.util.UUID;

/** One ledger line. Names are null when the product or location no longer resolves. */
public record StockMovementResult(
        UUID movementId,
        UUID productId,
        String productName,
        UUID locationId,
        String locationName,
        String movementType,
        long quantityDelta,
        long balanceAfter,
        String referenceType,
        UUID referenceId,
        UUID performedBy,
        Instant occurredAt,
        Instant recordedAt,
        String note
) {
    public static StockMovementResult from(StockMovement m, String productName, String locationName) {
        return new StockMovementResult(m.getMovementId(), m.getProductId(), productName, m.getLocationId(),
                locationName, m.getMovementType().name(), m.getQuantityDelta(), m.getBalanceAfter(),
                m.getReferenceType().name(), m.getReferenceId(), m.getPerformedBy(), m.getOccurredAt(),
                m.getRecordedAt(), m.getNote());
    }
}
