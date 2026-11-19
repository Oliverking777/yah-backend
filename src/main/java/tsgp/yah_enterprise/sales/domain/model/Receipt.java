package tsgp.yah_enterprise.sales.domain.model;

import tsgp.yah_enterprise.sales.domain.valueobject.ReceiptNumber;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** A small record: the printable content is built from the frozen order. One receipt per order. */
public class Receipt {

    private final UUID receiptId;
    private final ReceiptNumber receiptNumber;
    private final UUID orderId;
    private final Instant issuedAt;
    private final UUID issuedBy;

    private Receipt(UUID receiptId, ReceiptNumber receiptNumber, UUID orderId, Instant issuedAt, UUID issuedBy) {
        this.receiptId = Objects.requireNonNull(receiptId);
        this.receiptNumber = Objects.requireNonNull(receiptNumber);
        this.orderId = Objects.requireNonNull(orderId);
        this.issuedAt = Objects.requireNonNull(issuedAt);
        this.issuedBy = Objects.requireNonNull(issuedBy);
    }

    public static Receipt issue(ReceiptNumber number, UUID orderId, UUID issuedBy) {
        return new Receipt(UUID.randomUUID(), number, orderId, Instant.now(), issuedBy);
    }

    public static Receipt reconstitute(UUID receiptId, ReceiptNumber number, UUID orderId, Instant issuedAt,
                                       UUID issuedBy) {
        return new Receipt(receiptId, number, orderId, issuedAt, issuedBy);
    }

    public UUID getReceiptId() { return receiptId; }
    public ReceiptNumber getReceiptNumber() { return receiptNumber; }
    public UUID getOrderId() { return orderId; }
    public Instant getIssuedAt() { return issuedAt; }
    public UUID getIssuedBy() { return issuedBy; }
}
