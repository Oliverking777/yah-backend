package tsgp.yah_enterprise.pricing.presentation.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.pricing.application.dto.ProductPriceResult;

/**
 * HTTP response body describing one price.
 */
@Schema(description = "A price of one unit of a product")
public record ProductPriceResponse(

        @Schema(description = "Id of the price", example = "3f1c7a52-9d0e-4b6a-8c21-5e7d9a0b1c34")
        UUID priceId,

        @Schema(description = "Id of the product", example = "7a2b9c10-1d3e-4f5a-8b6c-9d0e1f2a3b4c")
        UUID productId,

        @Schema(description = "Id of the unit", example = "5e7d9a0b-1c34-4b6a-8c21-3f1c7a529d0e")
        UUID unitId,

        @Schema(description = "Price per unit, in whole FCFA", example = "500")
        long price,

        @Schema(description = "Smallest quantity this price applies to", example = "1")
        int minimumQuantity,

        @Schema(description = "Largest quantity this price applies to. Absent when there is no upper limit.", example = "5")
        Integer maximumQuantity,

        @Schema(description = "When the price starts (UTC)", example = "2026-10-04T08:00:00Z")
        Instant effectiveFrom,

        @Schema(description = "When the price ends (UTC, exclusive). Absent when open ended.", example = "2027-01-01T00:00:00Z")
        Instant effectiveTo,

        @Schema(description = "Whether the price can be used", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
        String priceStatus,

        @Schema(description = "When the price was created (UTC)", example = "2026-10-04T08:00:00Z")
        Instant createdAt,

        @Schema(description = "When the price was last changed (UTC)", example = "2026-10-04T08:00:00Z")
        Instant updatedAt
) {
    public static ProductPriceResponse from(ProductPriceResult result) {
        return new ProductPriceResponse(
                result.priceId(), result.productId(), result.unitId(), result.price(),
                result.minimumQuantity(), result.maximumQuantity(),
                result.effectiveFrom(), result.effectiveTo(), result.priceStatus(),
                result.createdAt(), result.updatedAt());
    }
}
