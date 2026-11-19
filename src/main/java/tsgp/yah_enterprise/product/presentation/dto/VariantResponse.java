package tsgp.yah_enterprise.product.presentation.dto;

import tsgp.yah_enterprise.product.application.dto.VariantResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** variantName is null for the unnamed variant of a simple product; fullName is what screens show. */
public record VariantResponse(
        UUID variantId, String variantName, String fullName, String sku, String variantStatus,
        List<VariantUnitResponse> units, Instant createdAt, Instant updatedAt) {

    public static VariantResponse from(VariantResult r) {
        return new VariantResponse(r.variantId(), r.variantName(), r.fullName(), r.sku(),
                r.variantStatus(), r.units().stream().map(VariantUnitResponse::from).toList(),
                r.createdAt(), r.updatedAt());
    }
}