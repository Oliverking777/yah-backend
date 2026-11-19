package tsgp.yah_enterprise.product.presentation.dto;

import tsgp.yah_enterprise.product.application.dto.ProductResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** simple: the product has its single unnamed variant, shown as a plain product (its units are the product's). */
public record ProductResponse(
        UUID productId, String productCode, String productName,
        String description, String productImageUrl, UUID categoryId, String productStatus,
        boolean simple, List<VariantResponse> variants, Instant createdAt, Instant updatedAt) {

    public static ProductResponse from(ProductResult r) {
        return new ProductResponse(r.productId(), r.productCode(), r.productName(),
                r.description(), r.productImageUrl(), r.categoryId(), r.productStatus(), r.simple(),
                r.variants().stream().map(VariantResponse::from).toList(),
                r.createdAt(), r.updatedAt());
    }
}
