package tsgp.yah_enterprise.payment.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Where the payment goes. Leave the list out to pay the customer's oldest debts first.")
public record AllocatePaymentRequest(
        @Size(max = 100, message = "At most 100 lines") List<@Valid @NotNull(message = "A line cannot be empty") AllocationLineRequest> allocations) {
}
