package tsgp.yah_enterprise.inventory.application.dto;

import tsgp.yah_enterprise.inventory.domain.model.StockLevel;

import java.time.Instant;
import java.util.UUID;

/** A row of the stock listing, with the product and location names for display. */
public record StockLevelResult(
        UUID productId,
        String productCode,
        String productName,
        UUID locationId,
        String locationName,
        long quantity,
        Long lowStockThreshold,
        boolean lowStock,
        Instant updatedAt
) {
    public static StockLevelResult from(StockLevel level, String productCode, String productName,
                                        String locationName) {
        return new StockLevelResult(level.getProductId(), productCode, productName, level.getLocationId(),
                locationName, level.getQuantity(), level.getLowStockThreshold(), level.isLowStock(),
                level.getUpdatedAt());
    }
}
