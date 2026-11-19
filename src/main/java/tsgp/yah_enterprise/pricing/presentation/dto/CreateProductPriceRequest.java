package tsgp.yah_enterprise.pricing.presentation.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tsgp.yah_enterprise.pricing.application.dto.CreateProductPriceCommand;

/**
 * HTTP request body for creating a price. The product comes from the URL.
 */
@Schema(description = "Data needed to create a price for one unit of a product")
public record CreateProductPriceRequest(

        @Schema(description = "Id of the unit this price is for (must be an active unit of the product)",
                example = "3f1c7a52-9d0e-4b6a-8c21-5e7d9a0b1c34", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Unit id is required")
        UUID unitId,

        @Schema(description = "Price per unit, in whole FCFA", example = "500",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than 0")
        Long price,

        @Schema(description = "Smallest quantity this price applies to. Use 1 for a flat price.", example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Minimum quantity is required")
        @Min(value = 1, message = "Minimum quantity must be at least 1")
        Integer minimumQuantity,

        @Schema(description = "Largest quantity this price applies to. Leave out for no upper limit.", example = "5")
        @Min(value = 1, message = "Maximum quantity must be at least 1")
        Integer maximumQuantity,

        @Schema(description = "When the price starts (UTC). Leave out for now.", example = "2026-10-04T08:00:00Z")
        Instant effectiveFrom,

        @Schema(description = "When the price ends (UTC, exclusive). Leave out for open ended.",
                example = "2027-01-01T00:00:00Z")
        Instant effectiveTo
) {
    public CreateProductPriceCommand toCommand(UUID productId) {
        return new CreateProductPriceCommand(productId, unitId, price, minimumQuantity, maximumQuantity,
                effectiveFrom, effectiveTo);
    }
}
