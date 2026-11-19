package tsgp.yah_enterprise.payment.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import tsgp.yah_enterprise.payment.application.dto.PaymentActor;
import tsgp.yah_enterprise.payment.application.dto.RecordPaymentCommand;
import tsgp.yah_enterprise.payment.domain.model.PaymentMethod;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Money received. The server assigns the PAY-000001 number. Never send card or wallet secrets.")
public record RecordPaymentRequest(
        @Schema(description = "Optional. An offline app sends its own UUID so a retry returns the same payment.")
        UUID paymentId,
        @NotNull(message = "Customer is required") UUID customerId,
        @Schema(description = "Optional. Defaults to your own location.") UUID locationId,
        @Schema(description = "Whole FCFA, at least 1") @Min(value = 1, message = "Amount must be at least 1 FCFA") long amount,
        @NotNull(message = "Payment method is required") PaymentMethod paymentMethod,
        @Schema(description = "Mobile money provider reference (3 to 50 characters). Must be empty for CASH.")
        @Size(max = 50, message = "Reference must be at most 50 characters") String reference,
        @Schema(description = "Optional. When the money was received (offline work); never in the future.") Instant receivedAt) {

    public RecordPaymentCommand toCommand(PaymentActor actor) {
        return new RecordPaymentCommand(paymentId, customerId, locationId, amount, paymentMethod, reference,
                receivedAt, actor);
    }
}
