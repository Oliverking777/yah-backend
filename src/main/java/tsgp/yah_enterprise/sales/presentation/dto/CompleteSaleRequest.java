package tsgp.yah_enterprise.sales.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.sales.application.dto.CompleteSaleCommand;
import tsgp.yah_enterprise.sales.application.dto.OrderActor;

import java.util.List;
import java.util.UUID;

@Schema(description = "The manager completes a PENDING order: what the customer pays now, plus optional discounts. Send only the amount kept (no change). The rest needs an approved credit.")
public record CompleteSaleRequest(
        @Size(max = 10, message = "At most 10 payments") List<@Valid @NotNull(message = "A payment cannot be empty") PaymentLineRequest> payments,
        @Schema(description = "The approved credit for this order, when the customer does not pay in full") UUID creditApprovalId,
        @Schema(description = "Optional discounts, one per line, in whole FCFA. Cannot be more than the line amount")
        @Size(max = 100, message = "At most 100 discounts") List<@Valid @NotNull(message = "A discount cannot be empty") DiscountRequest> discounts) {

    public CompleteSaleCommand toCommand(UUID orderId, OrderActor actor) {
        return new CompleteSaleCommand(orderId,
                payments == null ? List.of() : payments.stream().map(PaymentLineRequest::toCommand).toList(),
                creditApprovalId, actor,
                discounts == null ? List.of() : discounts.stream().map(DiscountRequest::toCommand).toList());
    }
}
