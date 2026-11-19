package tsgp.yah_enterprise.purchasing.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.purchasing.application.dto.PurchaseActor;
import tsgp.yah_enterprise.purchasing.application.dto.UpdatePurchaseCommand;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "A missing field is left unchanged. SUBMITTED purchases only.")
public record UpdatePurchaseRequest(UUID supplierId, UUID destinationLocationId, LocalDate expectedDeliveryDate,
        @Size(max = 500, message = "Note must be at most 500 characters") String note) {

    public UpdatePurchaseCommand toCommand(UUID purchaseId, PurchaseActor actor) {
        return new UpdatePurchaseCommand(purchaseId, supplierId, destinationLocationId, expectedDeliveryDate, note,
                actor);
    }
}
