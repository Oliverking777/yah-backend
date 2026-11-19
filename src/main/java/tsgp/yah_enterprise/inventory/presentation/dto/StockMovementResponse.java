package tsgp.yah_enterprise.inventory.presentation.dto;

import tsgp.yah_enterprise.inventory.application.dto.StockMovementResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "One line of the stock ledger")
public record StockMovementResponse(UUID movementId, UUID productId, String productName, UUID locationId,
                                    String locationName, String movementType, long quantityDelta,
                                    long balanceAfter, String referenceType, UUID referenceId,
                                    UUID performedBy, Instant occurredAt, Instant recordedAt, String note) {
    public static StockMovementResponse from(StockMovementResult r) {
        return new StockMovementResponse(r.movementId(), r.productId(), r.productName(), r.locationId(),
                r.locationName(), r.movementType(), r.quantityDelta(), r.balanceAfter(), r.referenceType(),
                r.referenceId(), r.performedBy(), r.occurredAt(), r.recordedAt(), r.note());
    }
}
