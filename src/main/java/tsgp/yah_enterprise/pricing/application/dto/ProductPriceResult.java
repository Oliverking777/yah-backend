package tsgp.yah_enterprise.pricing.application.dto;

import tsgp.yah_enterprise.pricing.domain.model.ProductPrice;

import java.time.Instant;
import java.util.UUID;

/** Output describing one price row. */
public record ProductPriceResult(
        UUID priceId,
        UUID productId,
        UUID unitId,
        long price,
        int minimumQuantity,
        Integer maximumQuantity,
        Instant effectiveFrom,
        Instant effectiveTo,
        String priceStatus,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductPriceResult from(ProductPrice price) {
        return new ProductPriceResult(
                price.getPriceId(),
                price.getProductId(),
                price.getUnitId(),
                price.getPrice().amount(),                    // unwrap the value object here
                price.getQuantityRange().min(),
                price.getQuantityRange().max(),
                price.getEffectiveFrom(),
                price.getEffectiveTo(),
                price.getPriceStatus().toString(),
                price.getCreatedAt(),
                price.getUpdatedAt());
    }
}
