package tsgp.yah_enterprise.pricing.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Input for creating a price.
 * A flat unit price is a price with minimumQuantity 1 and no maximumQuantity.
 * A null effectiveFrom means "now"; a null effectiveTo means "open ended".
 */
public record CreateProductPriceCommand(
        UUID productId,
        UUID unitId,
        long price,
        int minimumQuantity,
        Integer maximumQuantity,
        Instant effectiveFrom,
        Instant effectiveTo
) {
}
