package tsgp.yah_enterprise.sales.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tsgp.yah_enterprise.sales.application.dto.OrderActor;
import tsgp.yah_enterprise.sales.application.dto.UpdateOrderCommand;

import java.util.List;
import java.util.UUID;

@Schema(description = "Draft changes by its creator. A field left out is unchanged; items replaces all lines.")
public record UpdateOrderRequest(
        UUID customerId,
        @Size(max = 500, message = "Note must be at most 500 characters") String note,
        @Size(min = 1, max = 100, message = "1 to 100 lines") List<@Valid @NotNull(message = "A line cannot be empty") OrderLineRequest> items) {

    public UpdateOrderCommand toCommand(UUID orderId, OrderActor actor) {
        return new UpdateOrderCommand(orderId, customerId, note,
                items == null ? null : items.stream().map(OrderLineRequest::toCommand).toList(), actor);
    }
}
