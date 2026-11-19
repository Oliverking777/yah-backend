package tsgp.yah_enterprise.pricing.presentation.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.pricing.application.dto.ApplicablePriceResult;

/**
 * HTTP response body with the price that applies to a quantity.
 */
@Schema(description = "The price that applies to a quantity of one unit, and the line total")
public record ApplicablePriceResponse(

        @Schema(description = "Id of the price that was applied", example = "3f1c7a52-9d0e-4b6a-8c21-5e7d9a0b1c34")
        UUID priceId,

        @Schema(description = "Price per unit, in whole FCFA", example = "500")
        long unitPrice,

        @Schema(description = "The quantity that was priced", example = "3")
        int quantity,

        @Schema(description = "unitPrice x quantity, in whole FCFA", example = "1500")
        long lineTotal
) {
    public static ApplicablePriceResponse from(ApplicablePriceResult result) {
        return new ApplicablePriceResponse(result.priceId(), result.unitPrice(), result.quantity(), result.lineTotal());
    }
}
