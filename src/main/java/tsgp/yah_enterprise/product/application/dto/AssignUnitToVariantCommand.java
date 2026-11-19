package tsgp.yah_enterprise.product.application.dto;

import java.util.UUID;

/** 1 of unitId = conversionFactor base units of the variant (2 or more). */
public record AssignUnitToVariantCommand(UUID variantId, UUID unitId, int conversionFactor) {}
