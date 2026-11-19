package tsgp.yah_enterprise.inventory.presentation.dto;

import tsgp.yah_enterprise.inventory.application.dto.RequestStockAdjustmentCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Data needed to request a stock adjustment. It changes no stock until another user approves it.")
public record RequestStockAdjustmentRequest(
        @Schema(description = "Product to adjust", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Product is required")
        UUID productId,

        @Schema(description = "Location where the stock is", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Location is required")
        UUID locationId,

        @Schema(description = "INCREASE or DECREASE", example = "DECREASE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Direction is required")
        String direction,

        @Schema(description = "DAMAGE, LOSS, SUPPLIER_RETURN (decrease only), OPENING_STOCK (increase only), COUNT_CORRECTION or OTHER",
                example = "DAMAGE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Reason category is required")
        String reasonCategory,

        @Schema(description = "Whole BASE units (pieces), at least 1", example = "24",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Long quantity,

        @Schema(description = "Free text. Required when the reason is OTHER.", example = "Carton dropped during delivery")
        @Size(max = 500, message = "Note is too long")
        String note
) {
    public RequestStockAdjustmentCommand toCommand(UUID requestedBy, boolean restrictedToAssignedLocation) {
        return new RequestStockAdjustmentCommand(productId, locationId, direction, reasonCategory, quantity,
                note, requestedBy, restrictedToAssignedLocation);
    }
}
