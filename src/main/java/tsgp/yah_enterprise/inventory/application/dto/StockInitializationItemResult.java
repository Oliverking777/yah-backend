package tsgp.yah_enterprise.inventory.application.dto;

import tsgp.yah_enterprise.inventory.domain.model.StockInitializationItem;

import java.util.UUID;

/** One product line in a stock initialization result. */
public record StockInitializationItemResult(
        UUID itemId,
        UUID productId,
        String productName,
        UUID unitId,
        String unitCode,
        String unitName,
        int conversionFactor,
        long countedQuantity,
        long baseQuantity
) {
    public static StockInitializationItemResult from(StockInitializationItem item, String productName,
                                                     String unitCode, String unitName) {
        return new StockInitializationItemResult(item.getItemId(), item.getProductId(), productName,
                item.getUnitId(), unitCode, unitName, item.getConversionFactor(),
                item.getCountedQuantity(), item.getBaseQuantity());
    }
}
