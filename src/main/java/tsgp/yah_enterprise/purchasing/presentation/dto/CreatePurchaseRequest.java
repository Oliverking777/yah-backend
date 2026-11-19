package tsgp.yah_enterprise.purchasing.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.purchasing.application.dto.CreatePurchaseCommand;
import tsgp.yah_enterprise.purchasing.application.dto.PurchaseActor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "A new purchase, sent to the supplier (SUBMITTED). The total is computed by the server and the purchase is numbered PUR-000001.")
public record CreatePurchaseRequest(
        @Schema(description = "Optional. An offline app sends its own UUID so a retry returns the same purchase.") UUID purchaseId,
        @NotNull(message = "Supplier is required") UUID supplierId,
        @NotNull(message = "Destination location is required") UUID destinationLocationId,
        @Schema(description = "Today or later") LocalDate expectedDeliveryDate,
        @Size(max = 500, message = "Note must be at most 500 characters") String note,
        @NotEmpty(message = "At least one line is required") @Size(max = 100, message = "At most 100 lines")
        List<@Valid @NotNull(message = "A line cannot be empty") PurchaseLineRequest> items) {

    public CreatePurchaseCommand toCommand(PurchaseActor actor) {
        return new CreatePurchaseCommand(purchaseId, supplierId, destinationLocationId, expectedDeliveryDate, note,
                items.stream().map(PurchaseLineRequest::toCommand).toList(), actor);
    }
}
