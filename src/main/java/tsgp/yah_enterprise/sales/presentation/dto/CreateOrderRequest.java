package tsgp.yah_enterprise.sales.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.sales.application.dto.CreateOrderCommand;
import tsgp.yah_enterprise.sales.application.dto.OrderActor;

import java.util.List;
import java.util.UUID;

@Schema(description = "A new order (PENDING). No prices or totals are accepted: the server prices every line and numbers the order ORD-000001.")
public record CreateOrderRequest(
        @Schema(description = "Optional. An offline app sends its own UUID so a retry returns the same order.") UUID orderId,
        @NotNull(message = "Location is required") UUID locationId,
        @Schema(description = "Use the walk-in customer's id for an anonymous sale") @NotNull(message = "Customer is required") UUID customerId,
        @Size(max = 500, message = "Note must be at most 500 characters") String note,
        @NotEmpty(message = "At least one line is required") @Size(max = 100, message = "At most 100 lines")
        List<@Valid @NotNull(message = "A line cannot be empty") OrderLineRequest> items) {

    public CreateOrderCommand toCommand(OrderActor actor) {
        return new CreateOrderCommand(orderId, locationId, customerId, note,
                items.stream().map(OrderLineRequest::toCommand).toList(), actor);
    }
}
