package tsgp.yah_enterprise.product.presentation.dto;

import tsgp.yah_enterprise.product.application.dto.VariantSummary;

import java.util.List;
import java.util.UUID;

/** A variant with its product's names. usable: product and variant are both active. */
public record VariantSummaryResponse(
        UUID variantId, UUID productId, String productCode, String productName, String variantName,
        String fullName, String sku, boolean usable, UUID baseUnitId,
        List<VariantUnitResponse> units) {

    public static VariantSummaryResponse from(VariantSummary s) {
        return new VariantSummaryResponse(s.variantId(), s.productId(), s.productCode(), s.productName(),
                s.variantName(), s.fullName(), s.sku(), s.usable(), s.baseUnitId(),
                s.units().stream().map(VariantUnitResponse::from).toList());
    }
}
