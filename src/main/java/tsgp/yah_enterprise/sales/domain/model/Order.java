package tsgp.yah_enterprise.sales.domain.model;

import tsgp.yah_enterprise.sales.domain.exception.*;
import tsgp.yah_enterprise.sales.domain.valueobject.Money;
import tsgp.yah_enterprise.sales.domain.valueobject.OrderNumber;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

/**
 * An order becomes a sale only when it is COMPLETED. Creating an order never deducts stock or takes
 * payment. Every rule of the life cycle lives here; the service only calls the commands.
 * Totals are always computed from the lines, never accepted from the client.
 */
public class Order {

    public static final int MAX_LINES = 100;
    public static final int MIN_TEXT_LENGTH = 5;
    public static final int MAX_TEXT_LENGTH = 500;

    private final UUID orderId;
    private final OrderNumber orderNumber;
    private final UUID locationId;
    private UUID customerId;
    private OrderStatus orderStatus;
    private final List<OrderItem> items = new ArrayList<>();
    private Money subtotal = Money.ZERO;
    private Money discountTotal = Money.ZERO;
    private Money totalAmount = Money.ZERO;
    private Money amountPaid;
    private Money amountDue;
    private String note;
    private final UUID createdBy;
    private UUID validatedBy;
    private UUID completedBy;
    private UUID cancelledBy;
    private String rejectionReason;
    private String cancelReason;
    private final Instant createdAt;
    private Instant submittedAt;
    private Instant validatedAt;
    private Instant completedAt;
    private Instant updatedAt;
    private final List<OrderEvent> pendingEvents = new ArrayList<>();

    private Order(UUID orderId, OrderNumber orderNumber, UUID locationId, UUID customerId, OrderStatus orderStatus,
                  List<OrderItem> items, String note, UUID createdBy, UUID validatedBy, UUID completedBy,
                  UUID cancelledBy, String rejectionReason, String cancelReason, Money amountPaid, Money amountDue,
                  Instant createdAt, Instant submittedAt, Instant validatedAt, Instant completedAt,
                  Instant updatedAt) {
        this.orderId = Objects.requireNonNull(orderId);
        this.orderNumber = Objects.requireNonNull(orderNumber);
        this.locationId = Objects.requireNonNull(locationId);
        this.customerId = Objects.requireNonNull(customerId);
        this.orderStatus = Objects.requireNonNull(orderStatus);
        this.items.addAll(items);
        this.note = note;
        this.createdBy = Objects.requireNonNull(createdBy);
        this.validatedBy = validatedBy;
        this.completedBy = completedBy;
        this.cancelledBy = cancelledBy;
        this.rejectionReason = rejectionReason;
        this.cancelReason = cancelReason;
        this.amountPaid = amountPaid;
        this.amountDue = amountDue;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.submittedAt = submittedAt;
        this.validatedAt = validatedAt;
        this.completedAt = completedAt;
        this.updatedAt = Objects.requireNonNull(updatedAt);
        recalculate();
    }

    /** A new PENDING order. orderId is the client's UUID for offline work, or null. */
    public static Order create(UUID orderId, OrderNumber number, UUID locationId, UUID customerId, String note,
                               List<OrderItem> items, UUID createdBy) {
        if (locationId == null || customerId == null || createdBy == null) {
            throw new InvalidOrderException("Location, customer and the creating user are required");
        }
        validateItems(items);
        Instant now = Instant.now();
        Order order = new Order(orderId == null ? UUID.randomUUID() : orderId, number, locationId, customerId,
                OrderStatus.PENDING, items, cleanNote(note), createdBy, null, null, null, null, null, null, null,
                now, null, null, null, now);
        order.record(OrderEventType.CREATED, null, OrderStatus.PENDING, createdBy, null);
        return order;
    }

    public static Order reconstitute(UUID orderId, OrderNumber number, UUID locationId, UUID customerId,
                                     OrderStatus status, List<OrderItem> items, String note, UUID createdBy,
                                     UUID validatedBy, UUID completedBy, UUID cancelledBy, String rejectionReason,
                                     String cancelReason, Money amountPaid, Money amountDue, Instant createdAt,
                                     Instant submittedAt, Instant validatedAt, Instant completedAt,
                                     Instant updatedAt) {
        return new Order(orderId, number, locationId, customerId, status, items, note, createdBy, validatedBy,
                completedBy, cancelledBy, rejectionReason, cancelReason, amountPaid, amountDue, createdAt,
                submittedAt, validatedAt, completedAt, updatedAt);
    }

    // ------------------------------------------------------------------ editing (PENDING, creator only)

    /** Replaces all lines. A line for a product and unit already on the order keeps its row id. */
    public void replaceItems(UUID userId, List<OrderItem> newItems) {
        requireEditableBy(userId);
        validateItems(newItems);
        List<OrderItem> replaced = new ArrayList<>();
        for (OrderItem item : newItems) {
            OrderItem old = items.stream().filter(i -> i.sameLine(item.getProductId(), item.getUnitId()))
                    .findFirst().orElse(null);
            replaced.add(old == null ? item : item.withId(old.getOrderItemId()));
        }
        items.clear();
        items.addAll(replaced);
        recalculate();
        touch();
    }

    public void changeCustomer(UUID userId, UUID newCustomerId) {
        requireEditableBy(userId);
        if (newCustomerId == null) {
            throw new InvalidOrderException("Customer is required");
        }
        this.customerId = newCustomerId;
        touch();
    }

    public void changeNote(UUID userId, String newNote) {
        requireEditableBy(userId);
        this.note = cleanNote(newNote);
        touch();
    }

    /** Records that a draft was edited (one event per edit request). */
    public void markUpdated(UUID userId) {
        record(OrderEventType.UPDATED, orderStatus, orderStatus, userId, null);
    }

    /**
     * Sets the price of every line. The quoter returns null for a line that cannot be priced. Allowed only while PENDING
     * (estimate at creation, final price at completion). Frozen afterwards.
     */
    public void priceLines(Function<OrderItem, PriceQuote> quoter) {
        if (orderStatus != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Prices are frozen once the order is " + orderStatus);
        }
        for (OrderItem item : items) {
            item.price(quoter.apply(item));
        }
        recalculate();
        touch();
    }

    // ------------------------------------------------------------------ life cycle

    /**
     * Sets the discounts, one per line (a line not listed gets none). Allowed only while PENDING; the manager sends
     * them when completing the order. Every line must be priced first.
     */
    public void applyDiscounts(List<Discount> discounts) {
        requireStatus(OrderStatus.PENDING, "Only a pending order can be changed");
        requireAllPriced();
        Map<OrderItem, Money> planned = new LinkedHashMap<>();
        for (OrderItem item : items) {
            planned.put(item, Money.ZERO);
        }
        Set<String> seen = new HashSet<>();
        for (Discount d : discounts == null ? List.<Discount>of() : discounts) {
            if (d == null || d.productId() == null || d.unitId() == null || d.amount() == null) {
                throw new InvalidOrderException("A discount needs a product, a unit and an amount");
            }
            if (!seen.add(d.productId() + ":" + d.unitId())) {
                throw new InvalidDiscountException("A line can have only one discount");
            }
            OrderItem item = items.stream().filter(i -> i.sameLine(d.productId(), d.unitId())).findFirst()
                    .orElseThrow(() -> new InvalidOrderException("The discount is for a line that is not on the order"));
            if (d.amount().isGreaterThan(item.gross())) {
                throw new InvalidDiscountException("The discount " + d.amount() + " is more than the line amount "
                        + item.gross());
            }
            planned.put(item, d.amount());
        }
        planned.forEach(OrderItem::applyDiscount);
        recalculate();
        touch();
    }

    public void cancel(UUID userId, String reason) {
        if (orderStatus != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("An order that is " + orderStatus + " cannot be cancelled");
        }
        String cleaned = requireText(reason, "Cancel reason");
        OrderStatus from = orderStatus;
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelledBy = userId;
        this.cancelReason = cleaned;
        touch();
        record(OrderEventType.CANCELLED, from, orderStatus, userId, cleaned);
    }

    /** Throws unless the order can be completed now. The service calls it before touching stock or money. */
    public void requireCompletable() {
        requireStatus(OrderStatus.PENDING, "Only a pending order can be completed");
    }

    /** paid + due must equal the total. After this the order never changes. */
    public void complete(UUID userId, Money paid, Money due) {
        requireCompletable();
        if (!paid.plus(due).equals(totalAmount)) {
            throw new InvalidOrderException("Paid " + paid + " plus due " + due + " must equal the total "
                    + totalAmount);
        }
        OrderStatus from = orderStatus;
        this.orderStatus = OrderStatus.COMPLETED;
        this.amountPaid = paid;
        this.amountDue = due;
        this.completedBy = userId;
        this.completedAt = Instant.now();
        touch();
        record(OrderEventType.COMPLETED, from, orderStatus, userId, null);
    }

    // ------------------------------------------------------------------ reads

    public boolean isEditable() {
        return orderStatus == OrderStatus.PENDING;
    }

    /** Base quantity needed per product (several units of one product are added together). */
    public Map<UUID, Long> baseQuantityByProduct() {
        Map<UUID, Long> needed = new TreeMap<>();
        for (OrderItem item : items) {
            needed.merge(item.getProductId(), item.baseQuantity(), Long::sum);
        }
        return needed;
    }

    public List<OrderEvent> pendingEvents() { return List.copyOf(pendingEvents); }

    public void clearPendingEvents() { pendingEvents.clear(); }

    // ------------------------------------------------------------------ internals

    private void requireEditableBy(UUID userId) {
        requireStatus(OrderStatus.PENDING, "Only a pending order can be edited");
        if (!createdBy.equals(userId)) {
            throw new InvalidOrderStateException("Only the creator can edit this order");
        }
    }

    private void requireStatus(OrderStatus expected, String message) {
        if (orderStatus != expected) {
            throw new InvalidOrderStateException(message + " (this order is " + orderStatus + ")");
        }
    }

    private void requireAllPriced() {
        for (OrderItem item : items) {
            if (!item.isPriced()) {
                throw new OrderItemNotPriceableException("Product " + item.getProductId() + " has no price for "
                        + item.getQuantity() + " of unit " + item.getUnitId());
            }
        }
    }

    private void recalculate() {
        Money gross = Money.ZERO;
        Money discount = Money.ZERO;
        for (OrderItem item : items) {
            gross = gross.plus(item.gross());
            discount = discount.plus(item.getDiscountAmount());
        }
        this.subtotal = gross;
        this.discountTotal = discount;
        this.totalAmount = gross.minus(discount);
    }

    private static void validateItems(List<OrderItem> list) {
        if (list == null || list.isEmpty()) {
            throw new InvalidOrderException("An order needs at least one line");
        }
        if (list.size() > MAX_LINES) {
            throw new InvalidOrderException("An order has at most " + MAX_LINES + " lines");
        }
        Set<String> seen = new HashSet<>();
        for (OrderItem item : list) {
            if (!seen.add(item.getProductId() + ":" + item.getUnitId())) {
                throw new DuplicateOrderItemException("Product " + item.getProductId() + " with unit "
                        + item.getUnitId() + " is on the order twice");
            }
        }
    }

    private static String cleanNote(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.length() > MAX_TEXT_LENGTH) {
            throw new InvalidOrderException("Note must be at most " + MAX_TEXT_LENGTH + " characters");
        }
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static String requireText(String value, String label) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.length() < MIN_TEXT_LENGTH || cleaned.length() > MAX_TEXT_LENGTH) {
            throw new InvalidOrderException(label + " must be " + MIN_TEXT_LENGTH + " to " + MAX_TEXT_LENGTH
                    + " characters");
        }
        return cleaned;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private void record(OrderEventType type, OrderStatus from, OrderStatus to, UUID by, String eventNote) {
        pendingEvents.add(OrderEvent.record(orderId, type, from, to, by, eventNote));
    }

    public UUID getOrderId() { return orderId; }
    public OrderNumber getOrderNumber() { return orderNumber; }
    public UUID getLocationId() { return locationId; }
    public UUID getCustomerId() { return customerId; }
    public OrderStatus getOrderStatus() { return orderStatus; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
    public Money getSubtotal() { return subtotal; }
    public Money getDiscountTotal() { return discountTotal; }
    public Money getTotalAmount() { return totalAmount; }
    public Money getAmountPaid() { return amountPaid; }
    public Money getAmountDue() { return amountDue; }
    public String getNote() { return note; }
    public UUID getCreatedBy() { return createdBy; }
    public UUID getValidatedBy() { return validatedBy; }
    public UUID getCompletedBy() { return completedBy; }
    public UUID getCancelledBy() { return cancelledBy; }
    public String getRejectionReason() { return rejectionReason; }
    public String getCancelReason() { return cancelReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getValidatedAt() { return validatedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
