package tsgp.yah_enterprise.sales.application.dto;

import java.util.List;
import java.util.UUID;

/** A null field is left unchanged. items replaces all lines. */
public record UpdateOrderCommand(UUID orderId, UUID customerId, String note, List<OrderLineCommand> items,
                                 OrderActor actor) { }
