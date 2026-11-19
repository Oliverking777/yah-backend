package tsgp.yah_enterprise.inventory.domain.model;

import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockMovementException;

/**
 * Why stock moved. Quantities in the ledger are always base units.
 * IN types add stock, OUT types remove it, ADJUSTMENT may do either (it carries its own sign).
 */
public enum MovementType {
    PURCHASE(Direction.IN, ReferenceType.PURCHASE),
    TRANSFER_IN(Direction.IN, ReferenceType.TRANSFER),
    SALE_RETURN(Direction.IN, ReferenceType.SALE),
    TRANSFER_OUT(Direction.OUT, ReferenceType.TRANSFER),
    SALE(Direction.OUT, ReferenceType.SALE),
    ADJUSTMENT(Direction.EITHER, ReferenceType.ADJUSTMENT),
    INITIALIZATION(Direction.IN, ReferenceType.STOCK_INITIALIZATION);

    public enum Direction { IN, OUT, EITHER }

    private final Direction direction;
    private final ReferenceType referenceType;

    MovementType(Direction direction, ReferenceType referenceType) {
        this.direction = direction;
        this.referenceType = referenceType;
    }

    public Direction direction() { return direction; }

    /** The only kind of document that may justify this movement. */
    public ReferenceType requiredReferenceType() { return referenceType; }

    public static MovementType from(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidStockMovementException("Movement type is required");
        }
        try {
            return MovementType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStockMovementException("Unknown movement type: " + value);
        }
    }
}
