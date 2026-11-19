package tsgp.yah_enterprise.inventory.presentation.dto;

import tsgp.yah_enterprise.inventory.application.dto.StockLevelResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "A product's stock at a location, with names for display")
public record StockLevelResponse(UUID productId, String productCode, String productName, UUID locationId,
                                 String locationName, long quantity, Long lowStockThreshold, boolean lowStock,
                                 Instant updatedAt) {
    public static StockLevelResponse from(StockLevelResult r) {
        return new StockLevelResponse(r.productId(), r.productCode(), r.productName(), r.locationId(),
                r.locationName(), r.quantity(), r.lowStockThreshold(), r.lowStock(), r.updatedAt());
    }
}
