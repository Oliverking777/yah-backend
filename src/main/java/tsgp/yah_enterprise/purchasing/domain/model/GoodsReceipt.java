package tsgp.yah_enterprise.purchasing.domain.model;

import tsgp.yah_enterprise.purchasing.domain.exception.*;
import tsgp.yah_enterprise.purchasing.domain.valueobject.GoodsReceiptNumber;

import java.time.Instant;
import java.util.*;

/** The goods that arrived against a purchase. Only a CONFIRMED receipt adds stock. */
public class GoodsReceipt {

    public static final int MAX_LINES = 100;

    private final UUID goodsReceiptId;
    private final GoodsReceiptNumber receiptNumber;
    private final UUID purchaseId;
    private String deliveryNote;
    private ReceiptStatus receiptStatus;
    private final List<GoodsReceiptItem> items = new ArrayList<>();
    private final UUID receivedBy;
    private UUID confirmedBy;
    private UUID cancelledBy;
    private final Instant receivedAt;
    private Instant confirmedAt;
    private Instant cancelledAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private GoodsReceipt(UUID id, GoodsReceiptNumber number, UUID purchaseId, UUID receivedBy, Instant receivedAt,
                         Instant createdAt) {
        this.goodsReceiptId = id;
        this.receiptNumber = number;
        this.purchaseId = purchaseId;
        this.receivedBy = receivedBy;
        this.receivedAt = receivedAt;
        this.createdAt = createdAt;
    }

    public static GoodsReceipt create(UUID goodsReceiptId, GoodsReceiptNumber number, UUID purchaseId,
                                      String deliveryNote, Instant receivedAt, UUID receivedBy,
                                      List<GoodsReceiptItem> lines) {
        if (number == null || purchaseId == null || receivedBy == null) {
            throw new InvalidPurchaseException("Receipt number, purchase and receiving user are required");
        }
        Instant now = Instant.now();
        Instant arrived = receivedAt == null ? now : receivedAt;
        if (arrived.isAfter(now)) {
            throw new InvalidPurchaseException("The goods cannot have been received in the future");
        }
        GoodsReceipt r = new GoodsReceipt(goodsReceiptId == null ? UUID.randomUUID() : goodsReceiptId, number,
                purchaseId, receivedBy, arrived, now);
        r.deliveryNote = Text.optional(deliveryNote, 50, "The delivery note");
        r.receiptStatus = ReceiptStatus.DRAFT;
        r.updatedAt = now;
        r.setLines(lines);
        return r;
    }

    public static GoodsReceipt reconstitute(UUID id, GoodsReceiptNumber number, UUID purchaseId, String deliveryNote,
                                            ReceiptStatus status, List<GoodsReceiptItem> items, UUID receivedBy,
                                            UUID confirmedBy, UUID cancelledBy, Instant receivedAt,
                                            Instant confirmedAt, Instant cancelledAt, Instant createdAt,
                                            Instant updatedAt) {
        GoodsReceipt r = new GoodsReceipt(id, number, purchaseId, receivedBy, receivedAt, createdAt);
        r.deliveryNote = deliveryNote;
        r.receiptStatus = status;
        r.items.addAll(items);
        r.confirmedBy = confirmedBy;
        r.cancelledBy = cancelledBy;
        r.confirmedAt = confirmedAt;
        r.cancelledAt = cancelledAt;
        r.updatedAt = updatedAt;
        return r;
    }

    /** Replaces every line. DRAFT only. */
    public void replaceItems(List<GoodsReceiptItem> lines) {
        requireDraft("edited");
        setLines(lines);
        this.updatedAt = Instant.now();
    }

    /**
     * DRAFT to CONFIRMED. Who may confirm is a permission (RECEIPT_CONFIRM, checked by the controller):
     * the user who recorded the receipt may confirm it too.
     */
    public void confirm(UUID userId) {
        requireDraft("confirmed");
        if (userId == null) {
            throw new InvalidPurchaseException("The confirming user is required");
        }
        this.confirmedBy = userId;
        this.confirmedAt = Instant.now();
        this.receiptStatus = ReceiptStatus.CONFIRMED;
        this.updatedAt = confirmedAt;
    }

    public void cancel(UUID userId) {
        requireDraft("cancelled");
        if (userId == null) {
            throw new InvalidPurchaseException("The cancelling user is required");
        }
        this.cancelledBy = userId;
        this.cancelledAt = Instant.now();
        this.receiptStatus = ReceiptStatus.CANCELLED;
        this.updatedAt = cancelledAt;
    }

    /** Accepted quantity of a line in base units (accepted x the purchase line's conversion factor). */
    public static long acceptedBaseQuantity(GoodsReceiptItem item, int conversionFactor) {
        return (long) item.getQuantityAccepted() * conversionFactor;
    }

    public List<ReceiptLine> toLines() {
        return items.stream().map(GoodsReceiptItem::toLine).toList();
    }

    private void setLines(List<GoodsReceiptItem> lines) {
        if (lines == null || lines.isEmpty() || lines.size() > MAX_LINES) {
            throw new InvalidPurchaseException("A receipt has between 1 and " + MAX_LINES + " lines");
        }
        Set<UUID> seen = new HashSet<>();
        for (GoodsReceiptItem l : lines) {
            if (!seen.add(l.getPurchaseItemId())) {
                throw new DuplicatePurchaseItemException("A purchase line can appear only once on a receipt");
            }
        }
        items.clear();
        items.addAll(lines);
    }

    private void requireDraft(String what) {
        if (receiptStatus != ReceiptStatus.DRAFT) {
            throw new InvalidReceiptStateException("Receipt " + receiptNumber + " is " + receiptStatus
                    + ": only a DRAFT can be " + what);
        }
    }

    public UUID getGoodsReceiptId() { return goodsReceiptId; }
    public GoodsReceiptNumber getReceiptNumber() { return receiptNumber; }
    public UUID getPurchaseId() { return purchaseId; }
    public String getDeliveryNote() { return deliveryNote; }
    public ReceiptStatus getReceiptStatus() { return receiptStatus; }
    public List<GoodsReceiptItem> getItems() { return Collections.unmodifiableList(items); }
    public UUID getReceivedBy() { return receivedBy; }
    public UUID getConfirmedBy() { return confirmedBy; }
    public UUID getCancelledBy() { return cancelledBy; }
    public Instant getReceivedAt() { return receivedAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
