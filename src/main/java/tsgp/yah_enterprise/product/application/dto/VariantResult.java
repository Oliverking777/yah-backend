package tsgp.yah_enterprise.product.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A variant of a product. variantName is null for the unnamed variant of a simple product; fullName is what every
 * screen shows: "Indian Rice · 50 kg 10% broken", or just "Indian Rice".
 */
public record VariantResult(
        UUID variantId, String variantName, String fullName, String sku, String variantStatus,
        List<VariantUnitResult> units, Instant createdAt, Instant updatedAt) {

    public VariantResult { units = List.copyOf(units); }
}