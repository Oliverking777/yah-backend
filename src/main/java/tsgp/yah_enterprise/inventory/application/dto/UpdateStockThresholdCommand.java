package tsgp.yah_enterprise.inventory.application.dto;

import java.util.UUID;

/** lowStockThreshold null clears the threshold. */
public record UpdateStockThresholdCommand(
        UUID productId,
        UUID locationId,
        Long lowStockThreshold,
        UUID requestedBy,
        boolean restrictedToAssignedLocation
) {
}
