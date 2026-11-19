package tsgp.yah_enterprise.purchasing.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import tsgp.yah_enterprise.purchasing.application.dto.DirectPurchaseCommand;
import tsgp.yah_enterprise.purchasing.application.dto.DirectPurchaseLineCommand;
import tsgp.yah_enterprise.purchasing.application.dto.PurchaseActor;
import tsgp.yah_enterprise.purchasing.domain.model.RejectionType;

import java.util.List;
import java.util.UUID;

@Schema(description = "Goods bought and received on the spot. The purchase is created and confirmed at once, so the stock goes up immediately.")
public record DirectPurchaseRequest(
        @Schema(description = "Optional. The same id again returns what was already recorded.") UUID purchaseId,
        @NotNull(message = "Supplier is required") UUID supplierId,
        @NotNull(message = "Destination location is required") UUID destinationLocationId,
        @Schema(description = "The supplier's delivery note number")
        @Size(max = 50, message = "The delivery note must be at most 50 characters") String deliveryNote,
        @Size(max = 500, message = "Note must be at most 500 characters") String note,
        @NotEmpty(message = "At least one line is required") @Size(max = 100, message = "At most 100 lines")
        List<@Valid @NotNull(message = "A line cannot be empty") Line> items) {

    public record Line(
            @NotNull(message = "Product is required") UUID productId,
            @NotNull(message = "Unit is required") UUID unitId,
            @Schema(description = "What is kept: both the ordered and the accepted quantity")
            @Min(value = 1, message = "Received must be at least 1") int quantityReceived,
            @Schema(description = "Whole FCFA per unit")
            @Min(value = 1, message = "The price must be greater than zero") long unitPrice,
            @Schema(description = "Refused at the door. Needs rejectionType and rejectionReason when above 0.")
            @Min(value = 0, message = "Rejected cannot be negative") Integer quantityRejected,
            @Schema(description = "DAMAGED, WRONG_PRODUCT, EXCESS or OTHER") RejectionType rejectionType,
            @Size(max = 500, message = "The reason must be at most 500 characters") String rejectionReason) {
    }

    public DirectPurchaseCommand toCommand(PurchaseActor actor) {
        List<DirectPurchaseLineCommand> lines = items.stream().map(l -> new DirectPurchaseLineCommand(
                        l.productId(), l.unitId(), l.quantityReceived(), l.unitPrice(),
                        l.quantityRejected() == null ? 0 : l.quantityRejected(), l.rejectionType(), l.rejectionReason()))
                .toList();
        return new DirectPurchaseCommand(purchaseId, supplierId, destinationLocationId, deliveryNote, note, lines,
                actor);
    }
}
