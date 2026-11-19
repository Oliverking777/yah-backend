package tsgp.yah_enterprise.transfer.domain.exception;

/** 409: Inventory says there is not enough stock. Inventory's own exception never escapes. */
public class TransferStockUnavailableException extends TransferDomainException {
    public TransferStockUnavailableException(String message) {
        super(message);
    }
}
