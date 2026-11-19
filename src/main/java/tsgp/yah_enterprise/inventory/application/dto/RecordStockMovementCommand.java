package tsgp.yah_enterprise.inventory.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Input of RecordStockMovementUseCase, used by the Transfer, Sales and Purchasing modules.
 * quantity is a positive number of BASE units; the direction comes from movementType
 * (PURCHASE, TRANSFER_IN, SALE_RETURN add stock; TRANSFER_OUT, SALE remove it).
 * ADJUSTMENT is not accepted here: adjustments go through the adjustment use cases.
 * The same (referenceType, referenceId, productId, locationId, movementType) recorded twice
 * is applied once: the second call returns the first movement.
 */
public record RecordStockMovementCommand(
        UUID productId,
        UUID locationId,
        String movementType,
        long quantity,
        String referenceType,
        UUID referenceId,
        UUID performedBy,
        Instant occurredAt,
        String note
) {
}
