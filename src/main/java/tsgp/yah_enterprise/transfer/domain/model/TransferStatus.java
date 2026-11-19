package tsgp.yah_enterprise.transfer.domain.model;

/**
 * SUBMITTED: the goods have left the source and are on their way (in transit).
 * RECEIVED: the destination confirmed what arrived. Any shortfall or damage is recorded on the lines.
 * CANCELLED: the transfer was called off before receipt; the goods go back to the source.
 */
public enum TransferStatus {
    SUBMITTED, RECEIVED, CANCELLED;

    public boolean isFinal() {
        return this == RECEIVED || this == CANCELLED;
    }
}
