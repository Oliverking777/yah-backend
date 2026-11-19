package tsgp.yah_enterprise.sales.domain.repository;

import tsgp.yah_enterprise.sales.domain.model.Order;
import tsgp.yah_enterprise.sales.domain.model.OrderEvent;
import tsgp.yah_enterprise.sales.domain.model.OrderStatus;
import tsgp.yah_enterprise.sales.domain.valueobject.OrderNumber;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    /** Saves the order with its lines and appends its pending events. */
    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    /** Locks the row until the end of the transaction. */
    Optional<Order> findByIdForUpdate(UUID orderId);

    Optional<Order> findByNumber(OrderNumber number);

    /** Newest first. Any filter may be null. from is inclusive, to is exclusive. */
    PagedOrders findAll(int page, int size, UUID locationId, OrderStatus status, UUID customerId, UUID createdBy,
                        Instant from, Instant to);

    /** Completed orders only, completed between from (inclusive) and to (exclusive). */
    List<SummaryRow> summarize(Instant from, Instant to, SummaryGroupBy groupBy, UUID locationId);

    /** Oldest first. */
    List<OrderEvent> findEvents(UUID orderId);
}
