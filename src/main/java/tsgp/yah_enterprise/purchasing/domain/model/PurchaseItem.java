package tsgp.yah_enterprise.purchasing.domain.model;

import tsgp.yah_enterprise.purchasing.domain.exception.*;
import tsgp.yah_enterprise.purchasing.domain.valueobject.Money;

import java.util.UUID;

/**
 * A line of a purchase. The price and the conversion factor are snapshots: later changes never alter them.
 * The delivery figures (accepted, rejected, rejection type, reason) stay null until the purchase is confirmed.
 */
public class PurchaseItem {

    private final UUID purchaseItemId;
    private final UUID productId;
    private final UUID unitId;
    private final int conversionFactor;
    private int orderedQuantity;
    private Money unitPurchasePrice;
    private Integer acceptedQuantity;
    private Integer rejectedQuantity;
    private RejectionType rejectionType;
    private String discrepancyReason;

    private PurchaseItem(UUID purchaseItemId, UUID productId, UUID unitId, int conversionFactor, int orderedQuantity,
                         Money unitPurchasePrice, Integer acceptedQuantity, Integer rejectedQuantity,
                         RejectionType rejectionType, String discrepancyReason) {
        this.purchaseItemId = purchaseItemId;
        this.productId = productId;
        this.unitId = unitId;
        this.conversionFactor = conversionFactor;
        this.orderedQuantity = orderedQuantity;
        this.unitPurchasePrice = unitPurchasePrice;
        this.acceptedQuantity = acceptedQuantity;
        this.rejectedQuantity = rejectedQuantity;
        this.rejectionType = rejectionType;
        this.discrepancyReason = discrepancyReason;
    }

    public static PurchaseItem create(UUID productId, UUID unitId, int conversionFactor, int orderedQuantity,
                                      long unitPrice) {
        if (productId == null || unitId == null) {
            throw new InvalidPurchaseException("Product and unit are required");
        }
        if (conversionFactor < 1) {
            throw new InvalidPurchaseException("The conversion factor must be at least 1");
        }
        requireQuantity(orderedQuantity);
        return new PurchaseItem(UUID.randomUUID(), productId, unitId, conversionFactor, orderedQuantity,
                price(unitPrice), null, null, null, null);
    }

    public static PurchaseItem reconstitute(UUID purchaseItemId, UUID productId, UUID unitId, int conversionFactor,
                                            int orderedQuantity, long unitPrice, Integer acceptedQuantity,
                                            Integer rejectedQuantity, RejectionType rejectionType,
                                            String discrepancyReason) {
        return new PurchaseItem(purchaseItemId, productId, unitId, conversionFactor, orderedQuantity,
                Money.of(unitPrice), acceptedQuantity, rejectedQuantity, rejectionType, discrepancyReason);
    }

    static void requireQuantity(int quantity) {
        if (quantity < 1) {
            throw new InvalidPurchaseQuantityException("The quantity must be at least 1");
        }
    }

    static Money price(long unitPrice) {
        if (unitPrice <= 0) {
            throw new InvalidPurchasePriceException("The purchase price must be greater than zero");
        }
        return Money.of(unitPrice);
    }

    void change(int quantity, long unitPrice) {
        requireQuantity(quantity);
        this.unitPurchasePrice = price(unitPrice);
        this.orderedQuantity = quantity;
    }

    /** Checks what arrived for this line without changing anything. Returns the cleaned reason (null when none). */
    String checkReceipt(ReceiptLine line) {
        if (line.accepted() < 0 || line.rejected() < 0) {
            throw new InvalidReceiptQuantityException("Accepted and rejected quantities cannot be negative");
        }
        if (line.accepted() > orderedQuantity) {
            throw new OverReceiptException("Cannot accept " + line.accepted() + ": only " + orderedQuantity
                    + " were ordered on line " + purchaseItemId + ". Reject the extra goods as EXCESS");
        }
        if (line.rejected() > 0 && line.rejectionType() == null) {
            throw new InvalidReceiptQuantityException("A rejection type is required when goods are rejected");
        }
        boolean differs = line.rejected() > 0 || line.accepted() < orderedQuantity;
        if (differs) {
            return Text.required(line.reason(), 5, 500, "The reason for rejected or missing goods");
        }
        return Text.optional(line.reason(), 500, "The reason");
    }

    void applyReceipt(ReceiptLine line, String cleanedReason) {
        this.acceptedQuantity = line.accepted();
        this.rejectedQuantity = line.rejected();
        this.rejectionType = line.rejected() > 0 ? line.rejectionType() : null;
        this.discrepancyReason = cleanedReason;
    }

    /** True once confirmed with goods rejected or fewer accepted than ordered. */
    public boolean hasDiscrepancy() {
        return acceptedQuantity != null && (rejectedQuantity > 0 || acceptedQuantity < orderedQuantity);
    }

    /** Ordered but never delivered (neither accepted nor rejected). Null before confirmation, never negative. */
    public Integer missingQuantity() {
        if (acceptedQuantity == null) {
            return null;
        }
        return Math.max(0, orderedQuantity - acceptedQuantity - rejectedQuantity);
    }

    /** Accepted quantity in base units: what goes into stock. 0 before confirmation. */
    public long acceptedBaseQuantity() {
        return acceptedQuantity == null ? 0 : (long) acceptedQuantity * conversionFactor;
    }

    public Money lineTotal() {
        return unitPurchasePrice.times(orderedQuantity);
    }

    public UUID getPurchaseItemId() { return purchaseItemId; }
    public UUID getProductId() { return productId; }
    public UUID getUnitId() { return unitId; }
    public int getConversionFactor() { return conversionFactor; }
    public int getOrderedQuantity() { return orderedQuantity; }
    public Money getUnitPurchasePrice() { return unitPurchasePrice; }
    public Integer getAcceptedQuantity() { return acceptedQuantity; }
    public Integer getRejectedQuantity() { return rejectedQuantity; }
    public RejectionType getRejectionType() { return rejectionType; }
    public String getDiscrepancyReason() { return discrepancyReason; }
}
