package tsgp.yah_enterprise.inventory.presentation.dto;

import tsgp.yah_enterprise.inventory.application.dto.ProductStockResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Stock of one product at one location, in base units")
public record ProductStockResponse(UUID productId, UUID locationId, long quantity, Long lowStockThreshold,
                                   boolean lowStock, Instant updatedAt) {
    public static ProductStockResponse from(ProductStockResult r) {
        return new ProductStockResponse(r.productId(), r.locationId(), r.quantity(), r.lowStockThreshold(),
                r.lowStock(), r.updatedAt());
    }
}
