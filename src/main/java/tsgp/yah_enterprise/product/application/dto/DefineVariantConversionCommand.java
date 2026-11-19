package tsgp.yah_enterprise.product.application.dto;

import java.util.UUID;

public record DefineVariantConversionCommand(UUID variantId, UUID unitId, int conversionFactor) {}
