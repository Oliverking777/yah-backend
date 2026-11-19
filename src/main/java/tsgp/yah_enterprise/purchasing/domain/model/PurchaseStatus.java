package tsgp.yah_enterprise.purchasing.domain.model;

/**
 * SUBMITTED: ordered from the supplier, the goods have not arrived. Its lines can still be changed.
 * CONFIRMED: the delivery was counted; the accepted goods are in stock. Shortfalls and rejections stay on the lines.
 * CANCELLED: called off before anything arrived.
 */
public enum PurchaseStatus {
    SUBMITTED, CONFIRMED, CANCELLED;

    public boolean isFinal() {
        return this == CONFIRMED || this == CANCELLED;
    }
}
