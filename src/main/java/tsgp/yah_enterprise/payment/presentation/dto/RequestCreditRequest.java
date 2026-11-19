package tsgp.yah_enterprise.payment.presentation.dto;

import jakarta.validation.constraints.*;
import tsgp.yah_enterprise.payment.application.dto.PaymentActor;
import tsgp.yah_enterprise.payment.application.dto.RequestCreditCommand;

import java.util.UUID;

public record RequestCreditRequest(
        @NotNull(message = "Sale is required") UUID saleId,
        @NotNull(message = "Customer is required") UUID customerId,
        @Min(value = 1, message = "Amount must be at least 1 FCFA") long requestedAmount,
        @NotBlank(message = "Reason is required")
        @Size(min = 5, max = 500, message = "Reason must be 5 to 500 characters") String reason) {

    public RequestCreditCommand toCommand(PaymentActor actor) {
        return new RequestCreditCommand(saleId, customerId, requestedAmount, reason, actor);
    }
}
