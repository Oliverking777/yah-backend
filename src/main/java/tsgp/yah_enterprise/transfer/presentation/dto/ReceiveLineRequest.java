package tsgp.yah_enterprise.transfer.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.transfer.application.dto.ReceiveLineCommand;

import java.util.UUID;

public record ReceiveLineRequest(
        @NotNull(message = "Product id is required") UUID productId,
        @Schema(description = "Good quantity: goes into the destination stock")
        @NotNull(message = "Received quantity is required") @Min(value = 0, message = "Received quantity cannot be negative") Integer receivedQuantity,
        @Schema(description = "Damaged quantity: not added to stock. Defaults to 0.")
        @Min(value = 0, message = "Damaged quantity cannot be negative") Integer damagedQuantity,
        @Schema(description = "Required (5 to 500 characters) when goods are damaged or missing")
        @Size(max = 500, message = "Reason must be at most 500 characters") String discrepancyReason) {

    public ReceiveLineCommand toCommand() {
        return new ReceiveLineCommand(productId, receivedQuantity, damagedQuantity == null ? 0 : damagedQuantity,
                discrepancyReason);
    }
}
