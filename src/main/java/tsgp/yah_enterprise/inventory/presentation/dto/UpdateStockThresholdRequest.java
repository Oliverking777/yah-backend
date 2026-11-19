package tsgp.yah_enterprise.inventory.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Low-stock threshold of a product at a location")
public record UpdateStockThresholdRequest(
        @Schema(description = "Base units. The product is low when its stock is at or below this number. Null removes the threshold.",
                example = "50")
        @PositiveOrZero(message = "Low-stock threshold must not be negative")
        Long lowStockThreshold
) {
}
