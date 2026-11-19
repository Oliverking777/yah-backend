package tsgp.yah_enterprise.sales.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** One step in the life of an order. Appended on every transition; never edited or deleted. */
public class OrderEvent {

    private final UUID eventId;
    private final UUID orderId;
    private final OrderEventType eventType;
    private final OrderStatus fromStatus;
    private final OrderStatus toStatus;
    private final UUID performedBy;
    private final Instant occurredAt;
    private final String note;

    private OrderEvent(UUID eventId, UUID orderId, OrderEventType eventType, OrderStatus fromStatus,
                       OrderStatus toStatus, UUID performedBy, Instant occurredAt, String note) {
        this.eventId = Objects.requireNonNull(eventId);
        this.orderId = Objects.requireNonNull(orderId);
        this.eventType = Objects.requireNonNull(eventType);
        this.fromStatus = fromStatus;
        this.toStatus = Objects.requireNonNull(toStatus);
        this.performedBy = Objects.requireNonNull(performedBy);
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.note = note;
    }

    static OrderEvent record(UUID orderId, OrderEventType type, OrderStatus from, OrderStatus to, UUID by,
                             String note) {
        return new OrderEvent(UUID.randomUUID(), orderId, type, from, to, by, Instant.now(), note);
    }

    public static OrderEvent reconstitute(UUID eventId, UUID orderId, OrderEventType eventType,
                                          OrderStatus fromStatus, OrderStatus toStatus, UUID performedBy,
                                          Instant occurredAt, String note) {
        return new OrderEvent(eventId, orderId, eventType, fromStatus, toStatus, performedBy, occurredAt, note);
    }

    public UUID getEventId() { return eventId; }
    public UUID getOrderId() { return orderId; }
    public OrderEventType getEventType() { return eventType; }
    public OrderStatus getFromStatus() { return fromStatus; }
    public OrderStatus getToStatus() { return toStatus; }
    public UUID getPerformedBy() { return performedBy; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getNote() { return note; }
}
