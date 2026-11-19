package tsgp.yah_enterprise.inventory.application.dto;

import java.util.UUID;

/** One product line in a stock initialization command. countedQuantity is in the selected unit. */
public record InitializeStockLineCommand(
        UUID productId,
        UUID unitId,
        long countedQuantity
) {
}
