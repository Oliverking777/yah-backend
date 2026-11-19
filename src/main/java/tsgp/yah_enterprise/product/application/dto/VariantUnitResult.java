package tsgp.yah_enterprise.product.application.dto;

import tsgp.yah_enterprise.product.domain.model.VariantUnit;

import java.time.Instant;
import java.util.UUID;

/** A unit of a variant. unitCode and unitName are null when the unit cannot be found any more. */
public record VariantUnitResult(
        UUID variantUnitId, UUID unitId,
        String unitCode, String unitName,
        boolean baseUnit, int conversionFactor,
        String unitStatus, Instant createdAt, Instant updatedAt) {

    public static VariantUnitResult from(VariantUnit u, String unitCode, String unitName) {
        return new VariantUnitResult(u.getVariantUnitId(), u.getUnitId(), unitCode, unitName, u.isBaseUnit(),
                u.getConversionFactor(), u.getUnitStatus().name(), u.getCreatedAt(), u.getUpdatedAt());
    }
}
