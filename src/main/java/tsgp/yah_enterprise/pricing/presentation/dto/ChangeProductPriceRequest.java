package tsgp.yah_enterprise.pricing.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import tsgp.yah_enterprise.pricing.application.dto.ChangeProductPriceCommand;

/**
 * HTTP request body for changing a price. Fields left out stay unchanged.
 */
@Schema(description = "Changes to a price. The old price is kept in the history and a new one is created.")
public record ChangeProductPriceRequest(

        @Schema(description = "New price per unit, in whole FCFA. Leave out to keep the current amount.", example = "460")
        @Positive(message = "Price must be greater than 0")
        Long price,

        @Schema(description = "New smallest quantity. When given, the whole range is replaced.", example = "6")
        @Min(value = 1, message = "Minimum quantity must be at least 1")
        Integer minimumQuantity,

        @Schema(description = "New largest quantity. Leave out for no upper limit (only used with minimumQuantity).",
                example = "23")
        @Min(value = 1, message = "Maximum quantity must be at least 1")
        Integer maximumQuantity
) {
    public ChangeProductPriceCommand toCommand() {
        return new ChangeProductPriceCommand(price, minimumQuantity, maximumQuantity);
    }
}
