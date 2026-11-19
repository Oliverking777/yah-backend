package tsgp.yah_enterprise.purchasing.domain.model;

import tsgp.yah_enterprise.purchasing.domain.exception.*;

import java.time.Instant;
import java.util.UUID;

/** A difference between what was ordered and what was accepted. Resolving it never touches stock. */
public class PurchaseDiscrepancy {

    private final UUID discrepancyId;
    private final UUID purchaseId;
    private final UUID purchaseItemId;
    private final UUID goodsReceiptId;
    private final DiscrepancyType discrepancyType;
    private final int expectedQuantity;
    private final int actualQuantity;
    private final String reason;
    private ResolutionStatus resolutionStatus;
    private ResolutionType resolutionType;
    private String resolutionNote;
    private UUID resolvedBy;
    private Instant resolvedAt;
    private final Instant createdAt;

    private PurchaseDiscrepancy(UUID discrepancyId, UUID purchaseId, UUID purchaseItemId, UUID goodsReceiptId,
                                DiscrepancyType type, int expected, int actual, String reason,
                                ResolutionStatus status, ResolutionType resolutionType, String resolutionNote,
                                UUID resolvedBy, Instant resolvedAt, Instant createdAt) {
        this.discrepancyId = discrepancyId;
        this.purchaseId = purchaseId;
        this.purchaseItemId = purchaseItemId;
        this.goodsReceiptId = goodsReceiptId;
        this.discrepancyType = type;
        this.expectedQuantity = expected;
        this.actualQuantity = actual;
        this.reason = reason;
        this.resolutionStatus = status;
        this.resolutionType = resolutionType;
        this.resolutionNote = resolutionNote;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
        this.createdAt = createdAt;
    }

    static PurchaseDiscrepancy open(UUID purchaseId, UUID purchaseItemId, UUID goodsReceiptId, DiscrepancyType type,
                                    int expected, int actual, String reason) {
        return new PurchaseDiscrepancy(UUID.randomUUID(), purchaseId, purchaseItemId, goodsReceiptId, type, expected,
                actual, Text.required(reason, 5, 500, "The discrepancy reason"), ResolutionStatus.OPEN, null, null,
                null, null, Instant.now());
    }

    public static PurchaseDiscrepancy reconstitute(UUID discrepancyId, UUID purchaseId, UUID purchaseItemId,
                                                   UUID goodsReceiptId, DiscrepancyType type, int expected, int actual,
                                                   String reason, ResolutionStatus status, ResolutionType resolutionType,
                                                   String resolutionNote, UUID resolvedBy, Instant resolvedAt,
                                                   Instant createdAt) {
        return new PurchaseDiscrepancy(discrepancyId, purchaseId, purchaseItemId, goodsReceiptId, type, expected,
                actual, reason, status, resolutionType, resolutionNote, resolvedBy, resolvedAt, createdAt);
    }

    void resolve(ResolutionType type, String note, UUID userId) {
        if (resolutionStatus == ResolutionStatus.RESOLVED) {
            throw new DiscrepancyAlreadyResolvedException("Discrepancy " + discrepancyId + " is already resolved");
        }
        if (type == null) {
            throw new InvalidPurchaseException("The resolution type is required");
        }
        if (userId == null) {
            throw new InvalidPurchaseException("The resolving user is required");
        }
        this.resolutionNote = Text.required(note, 1, 500, "The resolution note");
        this.resolutionType = type;
        this.resolvedBy = userId;
        this.resolvedAt = Instant.now();
        this.resolutionStatus = ResolutionStatus.RESOLVED;
    }

    public UUID getDiscrepancyId() { return discrepancyId; }
    public UUID getPurchaseId() { return purchaseId; }
    public UUID getPurchaseItemId() { return purchaseItemId; }
    public UUID getGoodsReceiptId() { return goodsReceiptId; }
    public DiscrepancyType getDiscrepancyType() { return discrepancyType; }
    public int getExpectedQuantity() { return expectedQuantity; }
    public int getActualQuantity() { return actualQuantity; }
    public String getReason() { return reason; }
    public ResolutionStatus getResolutionStatus() { return resolutionStatus; }
    public ResolutionType getResolutionType() { return resolutionType; }
    public String getResolutionNote() { return resolutionNote; }
    public UUID getResolvedBy() { return resolvedBy; }
    public Instant getResolvedAt() { return resolvedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
