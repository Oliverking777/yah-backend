package tsgp.yah_enterprise.transfer.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tsgp.yah_enterprise.transfer.application.dto.TransferLineCommand;

import java.util.UUID;

@Schema(description = "One line of a transfer")
public record TransferLineRequest(
        @Schema(description = "Product to move") @NotNull(message = "Product id is required") UUID productId,
        @Schema(description = "Unit the quantity is counted in, for example carton") @NotNull(message = "Unit id is required") UUID unitId,
        @Schema(description = "Quantity in that unit, at least 1", example = "10") @Min(value = 1, message = "Quantity must be at least 1") int quantity) {

    public TransferLineCommand toCommand() {
        return new TransferLineCommand(productId, unitId, quantity);
    }
}
