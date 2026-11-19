package tsgp.yah_enterprise.product.presentation.dto;

import tsgp.yah_enterprise.product.application.dto.VariantUnitResult;

import java.time.Instant;
import java.util.UUID;

public record VariantUnitResponse(
        UUID variantUnitId, UUID unitId, String unitCode, String unitName,
        boolean baseUnit, int conversionFactor, String unitStatus,
        Instant createdAt, Instant updatedAt) {

    public static VariantUnitResponse from(VariantUnitResult r) {
        return new VariantUnitResponse(r.variantUnitId(), r.unitId(), r.unitCode(), r.unitName(),
                r.baseUnit(), r.conversionFactor(), r.unitStatus(), r.createdAt(), r.updatedAt());
    }
}
