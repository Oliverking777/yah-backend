package tsgp.yah_enterprise.product.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A product with its variants (oldest first). simple: one unnamed variant, shown as a plain product; its units are
 * the product's units on screen.
 */
public record ProductResult(
        UUID productId, String productCode, String productName,
        String description, String productImageUrl, UUID categoryId, String productStatus,
        boolean simple, List<VariantResult> variants, Instant createdAt, Instant updatedAt) {

    public ProductResult { variants = List.copyOf(variants); }
}
