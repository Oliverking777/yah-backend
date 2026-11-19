package tsgp.yah_enterprise.product.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.product.application.dto.AddVariantCommand;
import tsgp.yah_enterprise.product.application.dto.NewVariantCommand;

import java.util.List;
import java.util.UUID;

@Schema(description = "A new variant of a product with its units; its SKU is generated. A simple product's unnamed "
        + "variant must be named first.")
public record AddVariantRequest(
        @Schema(description = "e.g. \"25 kg 10% broken\"; unique within the product ignoring case and spaces")
        @NotBlank(message = "Variant name is required")
        @Size(max = 100, message = "Variant name must not exceed 100 characters")
        String variantName,

        @Schema(description = "e.g. Bag")
        @NotNull(message = "Base unit id is required")
        UUID baseUnitId,

        @Schema(description = "Its other units, each with how many base units it holds. May be empty.")
        @Size(max = 20, message = "A variant can have at most 20 other units")
        List<@Valid @NotNull AssignUnitToVariantRequest> units
) {
    public AddVariantCommand toCommand(UUID productId) {
        return new AddVariantCommand(productId, new NewVariantCommand(variantName, baseUnitId, units == null
                ? List.of()
                : units.stream().map(u -> new NewVariantCommand.Unit(u.unitId(), u.conversionFactor())).toList()));
    }
}
