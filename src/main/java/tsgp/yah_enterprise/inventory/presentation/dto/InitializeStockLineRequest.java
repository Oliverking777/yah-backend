package tsgp.yah_enterprise.inventory.presentation.dto;

import tsgp.yah_enterprise.inventory.application.dto.InitializeStockLineCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "One product line in a stock initialization request")
public record InitializeStockLineRequest(
        @Schema(description = "Product to initialize", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Product is required")
        UUID productId,

        @Schema(description = "Unit the quantity is counted in (must be a valid unit of the product)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Unit is required")
        UUID unitId,

        @Schema(description = "Physical count in the selected unit, at least 1", example = "50",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Counted quantity is required")
        @Min(value = 1, message = "Counted quantity must be at least 1")
        Long countedQuantity
) {
    public InitializeStockLineCommand toLineCommand() {
        return new InitializeStockLineCommand(productId, unitId, countedQuantity);
    }
}
