package tsgp.yah_enterprise.product.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.product.application.dto.NewVariantCommand;

import java.util.List;
import java.util.UUID;

@Schema(description = "A variant created with its product, with the units it is counted in. Its SKU is generated.")
public record NewVariantRequest(

        @Schema(description = "e.g. \"50 kg 10% broken\". Leave empty only for the single variant of a simple product.")
        @Size(max = 100, message = "Variant name must not exceed 100 characters")
        String variantName,

        @Schema(description = "The unit counted with factor 1, e.g. Bag")
        @NotNull(message = "Base unit id is required")
        UUID baseUnitId,

        @Schema(description = "Its other units, each with how many base units it holds. May be empty.")
        @Size(max = 20, message = "A variant can have at most 20 other units")
        List<@Valid @NotNull AssignUnitToVariantRequest> units
) {
    public NewVariantCommand toCommand() {
        return new NewVariantCommand(variantName, baseUnitId, units == null ? List.of()
                : units.stream().map(u -> new NewVariantCommand.Unit(u.unitId(), u.conversionFactor())).toList());
    }
}
