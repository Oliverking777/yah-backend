package tsgp.yah_enterprise.purchasing.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.purchasing.application.dto.ConfirmPurchaseCommand;
import tsgp.yah_enterprise.purchasing.application.dto.PurchaseActor;

import java.util.List;
import java.util.UUID;

@Schema(description = "The delivery of a SUBMITTED purchase. Send one line for every line of the purchase.")
public record ConfirmPurchaseRequest(
        @Schema(description = "The supplier's delivery note number")
        @Size(max = 50, message = "The delivery note must be at most 50 characters") String deliveryNote,
        @NotEmpty(message = "At least one line is required") @Size(max = 100, message = "At most 100 lines")
        List<@Valid @NotNull(message = "A line cannot be empty") DeliveryLineRequest> items) {

    public ConfirmPurchaseCommand toCommand(UUID purchaseId, PurchaseActor actor) {
        return new ConfirmPurchaseCommand(purchaseId, deliveryNote,
                items.stream().map(DeliveryLineRequest::toCommand).toList(), actor);
    }
}
