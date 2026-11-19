package tsgp.yah_enterprise.sales.application.dto;

import java.util.List;
import java.util.UUID;

/** orderId is the client's UUID for offline work (or null). It is also the saleId. */
public record CreateOrderCommand(UUID orderId, UUID locationId, UUID customerId, String note,
                                 List<OrderLineCommand> items, OrderActor actor) { }
