package tsgp.yah_enterprise.product.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * What other modules need to know about a variant they keep on a line, in stock or in a price: names to show,
 * whether it may be used now (product and variant active) and its units with their conversion factors.
 */
public record VariantSummary(
        UUID variantId, UUID productId, String productCode, String productName, String variantName,
        String fullName, String sku, boolean usable, UUID baseUnitId,
        List<VariantUnitResult> units) {

    public VariantSummary { units = List.copyOf(units); }
}