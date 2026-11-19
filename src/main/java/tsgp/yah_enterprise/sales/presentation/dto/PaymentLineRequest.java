package tsgp.yah_enterprise.sales.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.sales.application.dto.PaymentLineCommand;
import tsgp.yah_enterprise.sales.domain.model.SalesPaymentMethod;

public record PaymentLineRequest(
        @NotNull(message = "Payment method is required") SalesPaymentMethod paymentMethod,
        @Min(value = 1, message = "Amount must be at least 1 FCFA") long amount,
        @Size(max = 50, message = "Reference must be at most 50 characters") String reference) {

    public PaymentLineCommand toCommand() {
        return new PaymentLineCommand(paymentMethod, amount, reference);
    }
}
