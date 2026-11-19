package tsgp.yah_enterprise.payment.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tsgp.yah_enterprise.payment.application.dto.AllocationLine;

import java.util.UUID;

public record AllocationLineRequest(
        @NotNull(message = "Sale is required") UUID saleId,
        @Min(value = 1, message = "Amount must be at least 1 FCFA") long amount) {

    public AllocationLine toLine() {
        return new AllocationLine(saleId, amount);
    }
}
