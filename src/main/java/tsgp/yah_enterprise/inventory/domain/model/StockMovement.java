package tsgp.yah_enterprise.inventory.domain.model;

import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockMovementException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One line of the stock ledger. Immutable and append-only: a mistake is corrected by a new movement
 * (an adjustment), never by editing or deleting an old one. The quantity is signed base units.
 */
public class StockMovement {

    private static final int MAX_NOTE_LENGTH = 500;

    private final UUID movementId;
    private final UUID productId;
    private final UUID locationId;
    private final MovementType movementType;
    private final long quantityDelta;
    private final long balanceAfter;
    private final ReferenceType referenceType;
    private final UUID referenceId;
    private final UUID performedBy;
    private final Instant occurredAt;
    private final Instant recordedAt;
    private final String note;

    private StockMovement(UUID movementId, UUID productId, UUID locationId, MovementType movementType,
                          long quantityDelta, long balanceAfter, ReferenceType referenceType, UUID referenceId,
                          UUID performedBy, Instant occurredAt, Instant recordedAt, String note) {
        this.movementId = Objects.requireNonNull(movementId, "movementId must not be null");
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.locationId = Objects.requireNonNull(locationId, "locationId must not be null");
        this.movementType = Objects.requireNonNull(movementType, "movementType must not be null");
        this.quantityDelta = quantityDelta;
        this.balanceAfter = balanceAfter;
        this.referenceType = Objects.requireNonNull(referenceType, "referenceType must not be null");
        this.referenceId = Objects.requireNonNull(referenceId, "referenceId must not be null");
        this.performedBy = Objects.requireNonNull(performedBy, "performedBy must not be null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        this.recordedAt = Objects.requireNonNull(recordedAt, "recordedAt must not be null");
        this.note = note;
    }

    /**
     * Builds a new ledger line.
     *
     * @param quantityDelta signed base units; must be non-zero and agree with the movement type
     * @throws InvalidStockMovementException if the sign, the reference type or the note is wrong
     */
    public static StockMovement record(UUID productId, UUID locationId, MovementType movementType,
                                       long quantityDelta, long balanceAfter, ReferenceType referenceType,
                                       UUID referenceId, UUID performedBy, Instant occurredAt, String note) {
        Objects.requireNonNull(movementType, "movementType must not be null");
        Objects.requireNonNull(referenceType, "referenceType must not be null");
        if (quantityDelta == 0) {
            throw new InvalidStockMovementException("A stock movement must move at least one unit");
        }
        if (movementType.direction() == MovementType.Direction.IN && quantityDelta < 0) {
            throw new InvalidStockMovementException(movementType + " must add stock");
        }
        if (movementType.direction() == MovementType.Direction.OUT && quantityDelta > 0) {
            throw new InvalidStockMovementException(movementType + " must remove stock");
        }
        if (referenceType != movementType.requiredReferenceType()) {
            throw new InvalidStockMovementException(
                    movementType + " must reference a " + movementType.requiredReferenceType());
        }
        Instant now = Instant.now();
        return new StockMovement(UUID.randomUUID(), productId, locationId, movementType, quantityDelta,
                balanceAfter, referenceType, referenceId, performedBy,
                occurredAt == null ? now : occurredAt, now, validateNote(note));
    }

    public static StockMovement reconstitute(UUID movementId, UUID productId, UUID locationId,
                                             MovementType movementType, long quantityDelta, long balanceAfter,
                                             ReferenceType referenceType, UUID referenceId, UUID performedBy,
                                             Instant occurredAt, Instant recordedAt, String note) {
        return new StockMovement(movementId, productId, locationId, movementType, quantityDelta, balanceAfter,
                referenceType, referenceId, performedBy, occurredAt, recordedAt, note);
    }

    private static String validateNote(String note) {
        if (note == null) return null;
        String trimmed = note.trim();
        if (trimmed.length() > MAX_NOTE_LENGTH) {
            throw new InvalidStockMovementException("Note must not exceed " + MAX_NOTE_LENGTH + " characters");
        }
        return trimmed.isEmpty() ? null : trimmed;
    }

    public UUID getMovementId() { return movementId; }
    public UUID getProductId() { return productId; }
    public UUID getLocationId() { return locationId; }
    public MovementType getMovementType() { return movementType; }
    public long getQuantityDelta() { return quantityDelta; }
    public long getBalanceAfter() { return balanceAfter; }
    public ReferenceType getReferenceType() { return referenceType; }
    public UUID getReferenceId() { return referenceId; }
    public UUID getPerformedBy() { return performedBy; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getRecordedAt() { return recordedAt; }
    public String getNote() { return note; }

    @Override public boolean equals(Object o) {
        return o instanceof StockMovement other && movementId.equals(other.movementId);
    }
    @Override public int hashCode() { return movementId.hashCode(); }
}
