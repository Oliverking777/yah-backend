package tsgp.yah_enterprise.purchasing.domain.model;

import tsgp.yah_enterprise.purchasing.domain.exception.*;
import tsgp.yah_enterprise.purchasing.domain.valueobject.Money;
import tsgp.yah_enterprise.purchasing.domain.valueobject.PurchaseNumber;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * A purchase from a supplier, in one delivery.
 * <p>
 * Creating it orders the goods: it starts SUBMITTED and its lines may still be changed while nothing has arrived.
 * Confirming it records what arrived on every line (accepted, rejected, why) and makes it CONFIRMED; the
 * application service then adds the accepted goods to stock in the same transaction. A shortfall or a rejection
 * does not change the status: it sets {@link #hasDiscrepancy()} and stays on the lines. A SUBMITTED purchase may be
 * CANCELLED. CONFIRMED and CANCELLED are final.
 */
public class Purchase {

    public static final int MAX_LINES = 100;
    private static final ZoneOffset BUSINESS_ZONE = ZoneOffset.ofHours(1);

    private final UUID purchaseId;
    private final PurchaseNumber purchaseNumber;
    private UUID supplierId;
    private UUID destinationLocationId;
    private PurchaseStatus purchaseStatus;
    private boolean hasDiscrepancy;
    private final List<PurchaseItem> items = new ArrayList<>();
    private LocalDate expectedDeliveryDate;
    private String note;
    /** The supplier's delivery note number, recorded at confirmation. */
    private String deliveryNote;
    private final UUID createdBy;
    private UUID confirmedBy;
    private UUID cancelledBy;
    private String cancelReason;
    /** When the goods were ordered. */
    private final Instant createdAt;
    private Instant confirmedAt;
    private Instant cancelledAt;
    private Instant updatedAt;
    private long version;

    private Purchase(UUID purchaseId, PurchaseNumber number, UUID createdBy, Instant createdAt) {
        this.purchaseId = purchaseId;
        this.purchaseNumber = number;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    /** Orders the goods: the purchase starts SUBMITTED. purchaseId is the client's id for a safe retry, or null. */
    public static Purchase submit(UUID purchaseId, PurchaseNumber number, UUID supplierId, UUID destinationLocationId,
                                  LocalDate expectedDeliveryDate, String note, UUID createdBy,
                                  List<PurchaseItem> lines) {
        if (number == null) {
            throw new InvalidPurchaseException("The purchase number is required");
        }
        if (supplierId == null || destinationLocationId == null) {
            throw new InvalidPurchaseException("Supplier and destination location are required");
        }
        if (createdBy == null) {
            throw new InvalidPurchaseException("The creating user is required");
        }
        if (lines == null || lines.isEmpty()) {
            throw new InvalidPurchaseException("A purchase needs at least one line");
        }
        Instant now = Instant.now();
        Purchase p = new Purchase(purchaseId == null ? UUID.randomUUID() : purchaseId, number, createdBy, now);
        p.supplierId = supplierId;
        p.destinationLocationId = destinationLocationId;
        p.expectedDeliveryDate = checkExpectedDate(expectedDeliveryDate);
        p.note = Text.optional(note, 500, "The note");
        p.purchaseStatus = PurchaseStatus.SUBMITTED;
        p.updatedAt = now;
        lines.forEach(p::addLine);
        return p;
    }

    public static Purchase reconstitute(UUID purchaseId, PurchaseNumber number, UUID supplierId,
                                        UUID destinationLocationId, PurchaseStatus status, boolean hasDiscrepancy,
                                        List<PurchaseItem> items, LocalDate expectedDeliveryDate, String note,
                                        String deliveryNote, UUID createdBy, UUID confirmedBy, UUID cancelledBy,
                                        String cancelReason, Instant createdAt, Instant confirmedAt,
                                        Instant cancelledAt, Instant updatedAt, long version) {
        Purchase p = new Purchase(purchaseId, number, createdBy, createdAt);
        p.supplierId = supplierId;
        p.destinationLocationId = destinationLocationId;
        p.purchaseStatus = status;
        p.hasDiscrepancy = hasDiscrepancy;
        p.items.addAll(items);
        p.expectedDeliveryDate = expectedDeliveryDate;
        p.note = note;
        p.deliveryNote = deliveryNote;
        p.confirmedBy = confirmedBy;
        p.cancelledBy = cancelledBy;
        p.cancelReason = cancelReason;
        p.confirmedAt = confirmedAt;
        p.cancelledAt = cancelledAt;
        p.updatedAt = updatedAt;
        p.version = version;
        return p;
    }

    // ---- changes while the goods have not arrived (SUBMITTED only) ----

    /** A null argument leaves that detail unchanged. */
    public void changeDetails(UUID supplierId, UUID destinationLocationId, LocalDate expectedDeliveryDate,
                              String note) {
        requireSubmitted("changed");
        if (supplierId != null) this.supplierId = supplierId;
        if (destinationLocationId != null) this.destinationLocationId = destinationLocationId;
        if (expectedDeliveryDate != null) this.expectedDeliveryDate = checkExpectedDate(expectedDeliveryDate);
        if (note != null) this.note = Text.optional(note, 500, "The note");
        touch();
    }

    public void addItem(PurchaseItem item) {
        requireSubmitted("changed");
        addLine(item);
        touch();
    }

    public void changeItem(UUID purchaseItemId, int quantity, long unitPrice) {
        requireSubmitted("changed");
        findItem(purchaseItemId).change(quantity, unitPrice);
        touch();
    }

    public void removeItem(UUID purchaseItemId) {
        requireSubmitted("changed");
        PurchaseItem item = findItem(purchaseItemId);
        if (items.size() == 1) {
            throw new InvalidPurchaseException("A purchase needs at least one line: cancel it instead");
        }
        items.remove(item);
        touch();
    }

    // ---- life ----

    /**
     * Records the delivery: every line needs an entry. Per line, accepted cannot exceed what was ordered (extra goods
     * are rejected as EXCESS); a rejection needs a type; a reason is required when the line differs from the order.
     * Everything is checked before anything changes.
     */
    public void confirm(UUID userId, String deliveryNote, List<ReceiptLine> lines) {
        requireSubmitted("confirmed");
        if (userId == null) {
            throw new InvalidPurchaseException("The confirming user is required");
        }
        String cleanedNote = Text.optional(deliveryNote, 50, "The delivery note");
        Map<UUID, ReceiptLine> byItem = new HashMap<>();
        for (ReceiptLine l : lines == null ? List.<ReceiptLine>of() : lines) {
            if (l == null || l.purchaseItemId() == null) {
                throw new InvalidReceiptQuantityException("Every receipt line needs its purchase line");
            }
            findItem(l.purchaseItemId());
            if (byItem.put(l.purchaseItemId(), l) != null) {
                throw new DuplicatePurchaseItemException("A purchase line can appear only once in the delivery");
            }
        }
        Map<UUID, String> reasons = new HashMap<>();
        for (PurchaseItem item : items) {
            ReceiptLine l = byItem.get(item.getPurchaseItemId());
            if (l == null) {
                throw new InvalidReceiptQuantityException("The delivery is missing line " + item.getPurchaseItemId()
                        + ": say what arrived for every line, 0 if nothing");
            }
            reasons.put(item.getPurchaseItemId(), item.checkReceipt(l));
        }
        for (PurchaseItem item : items) {
            item.applyReceipt(byItem.get(item.getPurchaseItemId()), reasons.get(item.getPurchaseItemId()));
        }
        this.hasDiscrepancy = items.stream().anyMatch(PurchaseItem::hasDiscrepancy);
        this.deliveryNote = cleanedNote;
        this.confirmedBy = userId;
        this.confirmedAt = Instant.now();
        this.purchaseStatus = PurchaseStatus.CONFIRMED;
        this.updatedAt = confirmedAt;
    }

    /** Calls the purchase off before anything arrived. */
    public void cancel(UUID userId, String reason) {
        requireSubmitted("cancelled");
        String why = Text.required(reason, 5, 500, "The cancel reason");
        this.cancelReason = why;
        this.cancelledBy = requireUser(userId);
        this.cancelledAt = Instant.now();
        this.purchaseStatus = PurchaseStatus.CANCELLED;
        touch();
    }

    // ---- queries ----

    public PurchaseItem findItem(UUID purchaseItemId) {
        return items.stream().filter(i -> i.getPurchaseItemId().equals(purchaseItemId)).findFirst()
                .orElseThrow(() -> new PurchaseItemNotFoundException(
                        "Line " + purchaseItemId + " was not found on purchase " + purchaseNumber));
    }

    /** Lines may change only while the goods have not arrived. */
    public boolean isEditable() {
        return purchaseStatus == PurchaseStatus.SUBMITTED;
    }

    /** Received with goods rejected or missing. Always false before confirmation. */
    public boolean hasDiscrepancy() {
        return hasDiscrepancy;
    }

    public Money getTotalAmount() {
        Money total = Money.ZERO;
        for (PurchaseItem i : items) {
            total = total.plus(i.lineTotal());
        }
        return total;
    }

    // ---- helpers ----

    private void addLine(PurchaseItem item) {
        if (item == null) {
            throw new InvalidPurchaseException("A line cannot be empty");
        }
        if (items.size() >= MAX_LINES) {
            throw new InvalidPurchaseException("A purchase has at most " + MAX_LINES + " lines");
        }
        if (items.stream().anyMatch(i -> i.getProductId().equals(item.getProductId())
                && i.getUnitId().equals(item.getUnitId()))) {
            throw new DuplicatePurchaseItemException("This product and unit are already on the purchase");
        }
        items.add(item);
    }

    private static LocalDate checkExpectedDate(LocalDate date) {
        if (date != null && date.isBefore(LocalDate.now(BUSINESS_ZONE))) {
            throw new InvalidPurchaseException("The expected delivery date cannot be in the past");
        }
        return date;
    }

    private void requireSubmitted(String what) {
        if (purchaseStatus != PurchaseStatus.SUBMITTED) {
            throw new InvalidPurchaseStateException("Purchase " + purchaseNumber + " is " + purchaseStatus
                    + ": only a SUBMITTED purchase can be " + what);
        }
    }

    private static UUID requireUser(UUID userId) {
        if (userId == null) {
            throw new InvalidPurchaseException("The acting user is required");
        }
        return userId;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getPurchaseId() { return purchaseId; }
    public PurchaseNumber getPurchaseNumber() { return purchaseNumber; }
    public UUID getSupplierId() { return supplierId; }
    public UUID getDestinationLocationId() { return destinationLocationId; }
    public PurchaseStatus getPurchaseStatus() { return purchaseStatus; }
    public List<PurchaseItem> getItems() { return Collections.unmodifiableList(items); }
    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public String getNote() { return note; }
    public String getDeliveryNote() { return deliveryNote; }
    public UUID getCreatedBy() { return createdBy; }
    public UUID getConfirmedBy() { return confirmedBy; }
    public UUID getCancelledBy() { return cancelledBy; }
    public String getCancelReason() { return cancelReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}