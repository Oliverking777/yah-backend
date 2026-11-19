package tsgp.yah_enterprise.inventory.application.dto;

import tsgp.yah_enterprise.inventory.domain.model.StockLevel;

import java.time.Instant;
import java.util.UUID;

/** Stock of one product at one location, in base units. Zero when nothing was ever recorded. */
public record ProductStockResult(
        UUID productId,
        UUID locationId,
        long quantity,
        Long lowStockThreshold,
        boolean lowStock,
        Instant updatedAt
) {
    public static ProductStockResult from(StockLevel level) {
        return new ProductStockResult(level.getProductId(), level.getLocationId(), level.getQuantity(),
                level.getLowStockThreshold(), level.isLowStock(), level.getUpdatedAt());
    }

    public static ProductStockResult empty(UUID productId, UUID locationId) {
        return new ProductStockResult(productId, locationId, 0L, null, false, null);
    }
}
