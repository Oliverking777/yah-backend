package tsgp.yah_enterprise.sales.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tsgp.yah_enterprise.sales.application.dto.OrderLineCommand;

import java.util.UUID;

public record OrderLineRequest(
        @NotNull(message = "Product is required") UUID productId,
        @NotNull(message = "Unit is required") UUID unitId,
        @Min(value = 1, message = "Quantity must be at least 1") int quantity) {

    public OrderLineCommand toCommand() {
        return new OrderLineCommand(productId, unitId, quantity);
    }
}
