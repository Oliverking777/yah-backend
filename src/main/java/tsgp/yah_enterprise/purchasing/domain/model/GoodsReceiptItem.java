package tsgp.yah_enterprise.purchasing.domain.model;

import tsgp.yah_enterprise.purchasing.domain.exception.*;

import java.util.UUID;

public class GoodsReceiptItem {

    private final UUID goodsReceiptItemId;
    private final UUID purchaseItemId;
    private final int quantityDelivered;
    private final int quantityAccepted;
    private final int quantityRejected;
    private final RejectionType rejectionType;
    private final String rejectionReason;

    private GoodsReceiptItem(UUID id, UUID purchaseItemId, int delivered, int accepted, int rejected,
                             RejectionType type, String reason) {
        this.goodsReceiptItemId = id;
        this.purchaseItemId = purchaseItemId;
        this.quantityDelivered = delivered;
        this.quantityAccepted = accepted;
        this.quantityRejected = rejected;
        this.rejectionType = type;
        this.rejectionReason = reason;
    }

    public static GoodsReceiptItem create(UUID purchaseItemId, int delivered, int accepted, int rejected,
                                          RejectionType type, String reason) {
        if (purchaseItemId == null) {
            throw new InvalidReceiptQuantityException("The purchase line is required");
        }
        if (delivered < 1) {
            throw new InvalidReceiptQuantityException("The delivered quantity must be at least 1");
        }
        if (accepted < 0 || rejected < 0) {
            throw new InvalidReceiptQuantityException("Accepted and rejected quantities cannot be negative");
        }
        if (accepted + rejected != delivered) {
            throw new InvalidReceiptQuantityException("Accepted plus rejected must equal the delivered quantity");
        }
        String why = null;
        if (rejected > 0) {
            if (type == null) {
                throw new InvalidReceiptQuantityException("A rejection type is required when goods are rejected");
            }
            why = Text.required(reason, 5, 500, "The rejection reason");
        }
        return new GoodsReceiptItem(UUID.randomUUID(), purchaseItemId, delivered, accepted, rejected,
                rejected > 0 ? type : null, why);
    }

    public static GoodsReceiptItem reconstitute(UUID id, UUID purchaseItemId, int delivered, int accepted,
                                                int rejected, RejectionType type, String reason) {
        return new GoodsReceiptItem(id, purchaseItemId, delivered, accepted, rejected, type, reason);
    }

    public ReceiptLine toLine() {
        return new ReceiptLine(purchaseItemId, quantityAccepted, quantityRejected, rejectionType, rejectionReason);
    }

    public UUID getGoodsReceiptItemId() { return goodsReceiptItemId; }
    public UUID getPurchaseItemId() { return purchaseItemId; }
    public int getQuantityDelivered() { return quantityDelivered; }
    public int getQuantityAccepted() { return quantityAccepted; }
    public int getQuantityRejected() { return quantityRejected; }
    public RejectionType getRejectionType() { return rejectionType; }
    public String getRejectionReason() { return rejectionReason; }
}
