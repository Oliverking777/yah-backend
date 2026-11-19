package tsgp.yah_enterprise.sales.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import tsgp.yah_enterprise.sales.domain.port.SalesAuditPort;
import tsgp.yah_enterprise.sales.application.dto.*;
import tsgp.yah_enterprise.sales.domain.exception.*;
import tsgp.yah_enterprise.sales.domain.model.*;
import tsgp.yah_enterprise.sales.domain.port.*;
import tsgp.yah_enterprise.sales.domain.valueobject.Money;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.sales.application.port.in.*;
import tsgp.yah_enterprise.sales.domain.repository.*;
import tsgp.yah_enterprise.sales.domain.valueobject.OrderNumber;

import java.util.*;

/**
 * The life of a pending order (create, edit, cancel), plus reads. Creating an order never deducts stock or takes
 * payment: that happens only when the sale is completed (SaleCompletionService).
 */
@Service
@Transactional
public class OrderService implements CreateOrderUseCase, GetOrderUseCase, ListOrdersUseCase, UpdateOrderUseCase,
        CancelOrderUseCase, GetSaleReceiptUseCase, GetSalesSummaryUseCase, GetOrderHistoryUseCase {

    private final OrderRepository orders;
    private final SalesAuditPort audit;
    private final ReceiptRepository receipts;
    private final OrderNumberSequence numberSequence;
    private final SalesStockPort stock;
    private final OrderSupport support;

    /** Kept for callers that do not audit (the unit tests). The Spring constructor is the next one. */
    public OrderService(OrderRepository orders, ReceiptRepository receipts, OrderNumberSequence numberSequence,
                        SalesCustomerDirectoryPort customers, SalesProductCatalogPort products,
                        SalesPricingPort pricing, SalesLocationDirectoryPort locations, SalesStockPort stock) {
        this(orders, receipts, numberSequence, customers, products, pricing, locations, stock, SalesAuditPort.NOOP);
    }

    @Autowired
    public OrderService(OrderRepository orders, ReceiptRepository receipts, OrderNumberSequence numberSequence,
                        SalesCustomerDirectoryPort customers, SalesProductCatalogPort products,
                        SalesPricingPort pricing, SalesLocationDirectoryPort locations, SalesStockPort stock,
                        SalesAuditPort audit) {
        this.orders = orders;
        this.receipts = receipts;
        this.numberSequence = numberSequence;
        this.stock = stock;
        this.support = new OrderSupport(customers, products, pricing, locations);
        this.audit = audit;
    }

    @Override
    public OrderResult createOrder(CreateOrderCommand c) {
        if (c == null) {
            throw new InvalidOrderException("The order is required");
        }
        OrderActor actor = c.actor();
        OrderSupport.requireActor(actor);
        if (c.orderId() != null) {
            Optional<Order> existing = orders.findById(c.orderId());
            if (existing.isPresent()) {
                if (!actor.userId().equals(existing.get().getCreatedBy())) {
                    throw new OrderIdAlreadyUsedException("Order id " + c.orderId() + " is already used");
                }
                return OrderSupport.toResult(existing.get());
            }
        }
        if (c.locationId() == null || c.customerId() == null) {
            throw new InvalidOrderException("Location and customer are required");
        }
        support.requireAtLocation(actor, c.locationId());
        support.requireUsableLocation(c.locationId());
        support.requireUsableCustomer(c.customerId());
        List<OrderItem> lines = support.buildLines(c.items());
        Order order = Order.create(c.orderId(), OrderNumber.fromSequence(numberSequence.next()), c.locationId(),
                c.customerId(), c.note(), lines, actor.userId());
        order.priceLines(support::quote);
        return OrderSupport.toResult(orders.save(order));
    }

    @Override
    public OrderResult updateOrder(UpdateOrderCommand c) {
        Order order = lock(c.orderId());
        support.requireAtLocation(c.actor(), order.getLocationId());
        UUID userId = c.actor().userId();
        if (c.customerId() != null) {
            support.requireUsableCustomer(c.customerId());
            order.changeCustomer(userId, c.customerId());
        }
        if (c.note() != null) {
            order.changeNote(userId, c.note());
        }
        if (c.items() != null) {
            order.replaceItems(userId, support.buildLines(c.items()));
        }
        order.priceLines(support::quote);
        order.markUpdated(userId);
        return OrderSupport.toResult(orders.save(order));
    }

    @Override
    public OrderResult cancelOrder(CancelOrderCommand c) {
        Order order = lock(c.orderId());
        support.requireAtLocation(c.actor(), order.getLocationId());
        if (!c.canCancelAny() && !order.getCreatedBy().equals(c.actor().userId())) {
            throw new OrderLocationAccessException("You can cancel only the orders you created");
        }
        order.cancel(c.actor().userId(), c.reason());
        Order saved = orders.save(order);
        audit.record(new SalesAuditPort.AuditRecord(c.actor().userId(), "ORDER.CANCELLED", "ORDER", saved.getOrderId(), saved.getLocationId(), c.reason(),
                List.of(new SalesAuditPort.Change("orderStatus", null, "CANCELLED"))));
        return OrderSupport.toResult(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResult getOrder(UUID orderId, OrderActor actor) {
        Order order = orders.findById(orderId).orElseThrow(() -> notFound(orderId));
        support.requireAtLocation(actor, order.getLocationId());
        return OrderSupport.toResult(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderPageResult listOrders(OrderPageRequest r) {
        UUID scope = support.resolveScope(r.actor(), r.locationId());
        PagedOrders found = orders.findAll(r.page(), r.size(), scope, r.status(), r.customerId(), r.createdBy(),
                r.from(), r.to());
        return OrderPageResult.of(found.items().stream().map(OrderSupport::toResult).toList(), r.page(), r.size(),
                found.total());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderEventResult> getOrderHistory(UUID orderId, OrderActor actor) {
        Order order = orders.findById(orderId).orElseThrow(() -> notFound(orderId));
        support.requireAtLocation(actor, order.getLocationId());
        return orders.findEvents(orderId).stream().map(e -> new OrderEventResult(e.getEventId(),
                e.getEventType().name(), e.getFromStatus() == null ? null : e.getFromStatus().name(),
                e.getToStatus().name(), e.getPerformedBy(), e.getOccurredAt(), e.getNote())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptResult getSaleReceipt(UUID orderId, OrderActor actor) {
        Order order = orders.findById(orderId).orElseThrow(() -> notFound(orderId));
        support.requireAtLocation(actor, order.getLocationId());
        if (order.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new ReceiptNotAvailableException("Order " + order.getOrderNumber()
                    + " is not completed: it has no receipt");
        }
        Receipt receipt = receipts.findByOrderId(orderId).orElseThrow(() ->
                new ReceiptNotAvailableException("Order " + order.getOrderNumber() + " has no receipt"));
        return new ReceiptResult(receipt.getReceiptNumber().value(), orderId, order.getOrderNumber().value(),
                order.getCustomerId(), order.getLocationId(), receipt.getIssuedAt(), receipt.getIssuedBy(),
                order.getItems().stream().map(OrderSupport::toResult).toList(), order.getSubtotal().amount(),
                order.getDiscountTotal().amount(), order.getTotalAmount().amount(), order.getAmountPaid().amount(),
                order.getAmountDue().amount());
    }

    @Override
    @Transactional(readOnly = true)
    public SalesSummaryResult getSalesSummary(SalesSummaryRequest r) {
        if (r.from() == null || r.to() == null || !r.from().isBefore(r.to())) {
            throw new InvalidOrderException("A period is required: from must be before to");
        }
        SummaryGroupBy groupBy = r.groupBy() == null ? SummaryGroupBy.DAY : r.groupBy();
        UUID scope = support.resolveScope(r.actor(), r.locationId());
        List<SummaryRow> rows = orders.summarize(r.from(), r.to(), groupBy, scope);
        return new SalesSummaryResult(groupBy.name(),
                rows.stream().map(x -> new SalesSummaryResult.Row(x.key(), x.orderCount(), x.totalAmount())).toList(),
                rows.stream().mapToLong(SummaryRow::orderCount).sum(),
                rows.stream().mapToLong(SummaryRow::totalAmount).sum());
    }

    /** Advisory (F7): stock is read, not reserved. Refuses only a line that is clearly impossible. */
    private void checkStockAdvisory(Order order) {
        for (Map.Entry<UUID, Long> need : order.baseQuantityByProduct().entrySet()) {
            long available = stock.availableQuantity(need.getKey(), order.getLocationId());
            if (need.getValue() > available) {
                throw new OrderStockUnavailableException("Not enough " + support.productName(need.getKey())
                        + ": " + available + " available, " + need.getValue() + " needed");
            }
        }
    }

    private Order lock(UUID orderId) {
        return orders.findByIdForUpdate(orderId).orElseThrow(() -> notFound(orderId));
    }

    private static OrderNotFoundException notFound(UUID orderId) {
        return new OrderNotFoundException("Order " + orderId + " not found");
    }
}
