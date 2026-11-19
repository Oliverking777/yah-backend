package tsgp.yah_enterprise.inventory.domain.model;

import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockAdjustmentException;
import tsgp.yah_enterprise.inventory.domain.exception.StockAdjustmentAlreadyDecidedException;
import tsgp.yah_enterprise.inventory.domain.valueobject.AdjustmentNumber;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A request to correct stock (damage, loss, supplier return, count correction, opening stock).
 * It starts PENDING and changes no stock. A user allowed to decide (STOCK_ADJUST_APPROVE, checked by the
 * controller; the requester may decide too) approves it, which is the moment the stock moves, or rejects it.
 * Adjustments are never deleted (FR-039 to FR-041).
 */
public class StockAdjustment {

    private static final int MAX_NOTE_LENGTH = 500;
    private static final int MIN_REJECTION_NOTE_LENGTH = 5;

    private final UUID adjustmentId;
    private final AdjustmentNumber adjustmentNumber;
    private final UUID productId;
    private final UUID locationId;
    private final AdjustmentDirection direction;
    private final AdjustmentReason reasonCategory;
    private final long quantity;
    private final String note;
    private AdjustmentStatus adjustmentStatus;
    private final UUID createdBy;
    private UUID decidedBy;
    private String decisionNote;
    private Long beforeQuantity;
    private Long afterQuantity;
    private final Instant createdAt;
    private Instant decidedAt;
    private Instant updatedAt;

    private StockAdjustment(UUID adjustmentId, AdjustmentNumber adjustmentNumber, UUID productId, UUID locationId,
                            AdjustmentDirection direction, AdjustmentReason reasonCategory, long quantity,
                            String note, AdjustmentStatus adjustmentStatus, UUID createdBy, UUID decidedBy,
                            String decisionNote, Long beforeQuantity, Long afterQuantity, Instant createdAt,
                            Instant decidedAt, Instant updatedAt) {
        this.adjustmentId = Objects.requireNonNull(adjustmentId, "adjustmentId must not be null");
        this.adjustmentNumber = Objects.requireNonNull(adjustmentNumber, "adjustmentNumber must not be null");
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.locationId = Objects.requireNonNull(locationId, "locationId must not be null");
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
        this.reasonCategory = Objects.requireNonNull(reasonCategory, "reasonCategory must not be null");
        this.quantity = quantity;
        this.note = note;
        this.adjustmentStatus = Objects.requireNonNull(adjustmentStatus, "adjustmentStatus must not be null");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy must not be null");
        this.decidedBy = decidedBy;
        this.decisionNote = decisionNote;
        this.beforeQuantity = beforeQuantity;
        this.afterQuantity = afterQuantity;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.decidedAt = decidedAt;
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /**
     * Creates a PENDING adjustment.
     *
     * @param quantity base units, at least 1
     * @throws InvalidStockAdjustmentException if the quantity, reason/direction pair or note is invalid
     */
    public static StockAdjustment request(AdjustmentNumber adjustmentNumber, UUID productId, UUID locationId,
                                          AdjustmentDirection direction, AdjustmentReason reasonCategory,
                                          long quantity, String note, UUID requestedBy) {
        Objects.requireNonNull(direction, "direction must not be null");
        Objects.requireNonNull(reasonCategory, "reasonCategory must not be null");
        if (quantity < 1) {
            throw new InvalidStockAdjustmentException("Adjustment quantity must be at least 1");
        }
        if (!reasonCategory.allows(direction)) {
            throw new InvalidStockAdjustmentException(
                    reasonCategory + " cannot be used to " + direction.name().toLowerCase() + " stock");
        }
        String cleanNote = validateNote(note);
        if (reasonCategory == AdjustmentReason.OTHER && cleanNote == null) {
            throw new InvalidStockAdjustmentException("A note is required when the reason is OTHER");
        }
        Instant now = Instant.now();
        return new StockAdjustment(UUID.randomUUID(), adjustmentNumber, productId, locationId, direction,
                reasonCategory, quantity, cleanNote, AdjustmentStatus.PENDING, requestedBy,
                null, null, null, null, now, null, now);
    }

    public static StockAdjustment reconstitute(UUID adjustmentId, AdjustmentNumber adjustmentNumber, UUID productId,
                                               UUID locationId, AdjustmentDirection direction,
                                               AdjustmentReason reasonCategory, long quantity, String note,
                                               AdjustmentStatus adjustmentStatus, UUID createdBy, UUID decidedBy,
                                               String decisionNote, Long beforeQuantity, Long afterQuantity,
                                               Instant createdAt, Instant decidedAt, Instant updatedAt) {
        return new StockAdjustment(adjustmentId, adjustmentNumber, productId, locationId, direction, reasonCategory,
                quantity, note, adjustmentStatus, createdBy, decidedBy, decisionNote, beforeQuantity, afterQuantity,
                createdAt, decidedAt, updatedAt);
    }

    /** Signed base units this adjustment adds to (positive) or removes from (negative) the stock. */
    public long signedQuantity() {
        return direction.sign() * quantity;
    }

    /**
     * Approves the adjustment. The caller applies the stock movement in the same transaction and
     * passes the level it found, so the before/after snapshot (FR-040) is stored with the decision.
     *
     * @throws StockAdjustmentAlreadyDecidedException       if it is not PENDING
     */
    public void approve(UUID approverId, long quantityBefore, String decisionNote) {
        ensureCanBeDecidedBy(approverId);
        this.adjustmentStatus = AdjustmentStatus.APPROVED;
        this.decidedBy = approverId;
        this.decisionNote = validateNote(decisionNote);
        this.beforeQuantity = quantityBefore;
        this.afterQuantity = quantityBefore + signedQuantity();
        this.decidedAt = Instant.now();
        touch();
    }

    /**
     * Rejects the adjustment. A reason of 5 to 500 characters is required.
     *
     * @throws StockAdjustmentAlreadyDecidedException       if it is not PENDING
     * @throws InvalidStockAdjustmentException              if the reason is missing or too short
     */
    public void reject(UUID deciderId, String reason) {
        ensureCanBeDecidedBy(deciderId);
        String cleanReason = validateNote(reason);
        if (cleanReason == null || cleanReason.length() < MIN_REJECTION_NOTE_LENGTH) {
            throw new InvalidStockAdjustmentException(
                    "A rejection reason of at least " + MIN_REJECTION_NOTE_LENGTH + " characters is required");
        }
        this.adjustmentStatus = AdjustmentStatus.REJECTED;
        this.decidedBy = deciderId;
        this.decisionNote = cleanReason;
        this.decidedAt = Instant.now();
        touch();
    }

    public boolean isPending() { return adjustmentStatus == AdjustmentStatus.PENDING; }

    /**
     * Checks that this adjustment may still be decided, without changing anything.
     * Called before the stock moves, so a refused approval never touches the ledger.
     * The requester may decide their own adjustment: who may decide is a permission (STOCK_ADJUST_APPROVE).
     *
     * @throws StockAdjustmentAlreadyDecidedException       if it is not PENDING
     */
    public void ensureCanBeDecidedBy(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        if (adjustmentStatus != AdjustmentStatus.PENDING) {
            throw new StockAdjustmentAlreadyDecidedException(adjustmentId, adjustmentStatus.name());
        }
    }

    private void touch() { this.updatedAt = Instant.now(); }

    private static String validateNote(String note) {
        if (note == null) return null;
        String trimmed = note.trim();
        if (trimmed.length() > MAX_NOTE_LENGTH) {
            throw new InvalidStockAdjustmentException("Note must not exceed " + MAX_NOTE_LENGTH + " characters");
        }
        return trimmed.isEmpty() ? null : trimmed;
    }

    public UUID getAdjustmentId() { return adjustmentId; }
    public AdjustmentNumber getAdjustmentNumber() { return adjustmentNumber; }
    public UUID getProductId() { return productId; }
    public UUID getLocationId() { return locationId; }
    public AdjustmentDirection getDirection() { return direction; }
    public AdjustmentReason getReasonCategory() { return reasonCategory; }
    public long getQuantity() { return quantity; }
    public String getNote() { return note; }
    public AdjustmentStatus getAdjustmentStatus() { return adjustmentStatus; }
    public UUID getCreatedBy() { return createdBy; }
    public UUID getDecidedBy() { return decidedBy; }
    public String getDecisionNote() { return decisionNote; }
    public Long getBeforeQuantity() { return beforeQuantity; }
    public Long getAfterQuantity() { return afterQuantity; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override public boolean equals(Object o) {
        return o instanceof StockAdjustment other && adjustmentId.equals(other.adjustmentId);
    }
    @Override public int hashCode() { return adjustmentId.hashCode(); }
}