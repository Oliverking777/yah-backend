package tsgp.yah_enterprise.inventory.application.dto;

import java.util.UUID;

/** quantity is a positive number of base units; direction is INCREASE or DECREASE. */
public record RequestStockAdjustmentCommand(
        UUID productId,
        UUID locationId,
        String direction,
        String reasonCategory,
        long quantity,
        String note,
        UUID requestedBy,
        boolean restrictedToAssignedLocation
) {
}
