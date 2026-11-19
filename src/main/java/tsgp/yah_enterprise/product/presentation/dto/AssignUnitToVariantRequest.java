package tsgp.yah_enterprise.product.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tsgp.yah_enterprise.product.application.dto.AssignUnitToVariantCommand;

import java.util.UUID;

public record AssignUnitToVariantRequest(
        @NotNull(message = "Unit id is required") UUID unitId,
        @Schema(description = "1 of this unit = N base units of the variant")
        @Min(value = 2, message = "Conversion factor must be >= 2") int conversionFactor
) {
    public AssignUnitToVariantCommand toCommand(UUID variantId) {
        return new AssignUnitToVariantCommand(variantId, unitId, conversionFactor);
    }
}
