package tsgp.yah_enterprise.transfer.domain.model;

import tsgp.yah_enterprise.transfer.domain.exception.InvalidTransferException;
import tsgp.yah_enterprise.transfer.domain.exception.InvalidTransferQuantityException;

import java.util.Objects;
import java.util.UUID;

/**
 * One line of a transfer: a product sent in the unit the employee chose (for example cartons).
 * The conversion factor is copied from the product when the line is added, so a later change of the
 * conversion never alters an old document (F12). Stock effects always use the base quantity.
 */
public class TransferItem {

    public static final int MIN_TEXT_LENGTH = 5;
    public static final int MAX_TEXT_LENGTH = 500;

    private final UUID transferItemId;
    private final UUID productId;
    private final UUID unitId;
    private final int conversionFactor;
    /** Quantity sent, in the line's unit. Fixed when the transfer is submitted. */
    private final int quantity;
    private Integer receivedQuantity;
    private Integer damagedQuantity;
    private String discrepancyReason;

    private TransferItem(UUID transferItemId, UUID productId, UUID unitId, int conversionFactor, int quantity,
                         Integer receivedQuantity, Integer damagedQuantity, String discrepancyReason) {
        this.transferItemId = Objects.requireNonNull(transferItemId, "transferItemId");
        if (productId == null || unitId == null) {
            throw new InvalidTransferException("Every line needs a product and a unit");
        }
        if (conversionFactor < 1) {
            throw new InvalidTransferException("Conversion factor must be at least 1");
        }
        if (quantity < 1) {
            throw new InvalidTransferQuantityException("Quantity must be at least 1");
        }
        this.productId = productId;
        this.unitId = unitId;
        this.conversionFactor = conversionFactor;
        this.quantity = quantity;
        this.receivedQuantity = receivedQuantity;
        this.damagedQuantity = damagedQuantity;
        this.discrepancyReason = discrepancyReason;
    }

    /** A new line with a new id. */
    public static TransferItem newLine(UUID productId, UUID unitId, int conversionFactor, int quantity) {
        return new TransferItem(UUID.randomUUID(), productId, unitId, conversionFactor, quantity, null, null, null);
    }

    public static TransferItem reconstitute(UUID transferItemId, UUID productId, UUID unitId, int conversionFactor,
                                            int quantity, Integer receivedQuantity, Integer damagedQuantity,
                                            String discrepancyReason) {
        return new TransferItem(transferItemId, productId, unitId, conversionFactor, quantity,
                receivedQuantity, damagedQuantity, discrepancyReason);
    }

    /** Base quantity of any quantity of this line: quantity x conversion factor. */
    public long baseQuantity(int anyQuantity) {
        return (long) anyQuantity * conversionFactor;
    }

    /** Base quantity that was sent. */
    public long sentBaseQuantity() {
        return baseQuantity(quantity);
    }

    /** Sent - received - damaged, once the line has been received. Null before that. */
    public Integer missingQuantity() {
        if (receivedQuantity == null) {
            return null;
        }
        return quantity - receivedQuantity - (damagedQuantity == null ? 0 : damagedQuantity);
    }

    /** Checks a receipt without changing anything. Returns the cleaned reason (null when none is needed). */
    String checkReceipt(int received, int damaged, String reason) {
        if (received < 0 || damaged < 0) {
            throw new InvalidTransferQuantityException("Received and damaged quantities cannot be negative");
        }
        if ((long) received + damaged > quantity) {
            throw new InvalidTransferQuantityException("Product " + productId + ": received " + received
                    + " + damaged " + damaged + " is more than the " + quantity + " sent");
        }
        String cleaned = reason == null ? "" : reason.trim();
        boolean needsReason = damaged > 0 || received + damaged < quantity;
        if (needsReason) {
            if (cleaned.length() < MIN_TEXT_LENGTH || cleaned.length() > MAX_TEXT_LENGTH) {
                throw new InvalidTransferException("Product " + productId + ": a reason of " + MIN_TEXT_LENGTH
                        + " to " + MAX_TEXT_LENGTH + " characters is required for damaged or missing goods");
            }
            return cleaned;
        }
        if (cleaned.length() > MAX_TEXT_LENGTH) {
            throw new InvalidTransferException("Reason is too long");
        }
        return cleaned.isEmpty() ? null : cleaned;
    }

    void applyReceipt(int received, int damaged, String cleanedReason) {
        this.receivedQuantity = received;
        this.damagedQuantity = damaged;
        this.discrepancyReason = cleanedReason;
    }

    public boolean isFullyReceived() {
        return receivedQuantity != null && receivedQuantity == quantity;
    }

    /** Received, but not everything arrived in good condition. */
    public boolean hasDiscrepancy() {
        return receivedQuantity != null && receivedQuantity != quantity;
    }

    public UUID getTransferItemId() { return transferItemId; }
    public UUID getProductId() { return productId; }
    public UUID getUnitId() { return unitId; }
    public int getConversionFactor() { return conversionFactor; }
    public int getQuantity() { return quantity; }
    public Integer getReceivedQuantity() { return receivedQuantity; }
    public Integer getDamagedQuantity() { return damagedQuantity; }
    public String getDiscrepancyReason() { return discrepancyReason; }
}