package tsgp.yah_enterprise.purchasing.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.purchasing.application.dto.DeliveryLineCommand;
import tsgp.yah_enterprise.purchasing.domain.model.RejectionType;

import java.util.UUID;

@Schema(description = "What arrived for one purchase line, in the line's unit. Whatever is neither accepted nor rejected counts as missing.")
public record DeliveryLineRequest(
        @NotNull(message = "The purchase line is required") UUID purchaseItemId,
        @Schema(description = "Goes into stock. At most the ordered quantity.")
        @Min(value = 0, message = "Accepted cannot be negative") int quantityAccepted,
        @Schema(description = "Refused at the door. Does not go into stock.")
        @Min(value = 0, message = "Rejected cannot be negative") int quantityRejected,
        @Schema(description = "Required when something is rejected: DAMAGED, WRONG_PRODUCT, EXCESS or OTHER")
        RejectionType rejectionType,
        @Schema(description = "Required when the line differs from the order (rejected or missing goods)")
        @Size(max = 500, message = "The reason must be at most 500 characters") String reason) {

    public DeliveryLineCommand toCommand() {
        return new DeliveryLineCommand(purchaseItemId, quantityAccepted, quantityRejected, rejectionType, reason);
    }
}
