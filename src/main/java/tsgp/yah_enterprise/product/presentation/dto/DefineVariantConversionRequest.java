package tsgp.yah_enterprise.product.presentation.dto;

import jakarta.validation.constraints.Min;
import tsgp.yah_enterprise.product.application.dto.DefineVariantConversionCommand;

import java.util.UUID;

public record DefineVariantConversionRequest(
        @Min(value = 2, message = "Conversion factor must be >= 2") int conversionFactor
) {
    public DefineVariantConversionCommand toCommand(UUID variantId, UUID unitId) {
        return new DefineVariantConversionCommand(variantId, unitId, conversionFactor);
    }
}
