package tsgp.yah_enterprise.transfer.domain.exception;

/** 409: a product appears twice in one transfer. */
public class DuplicateTransferItemException extends TransferDomainException {
    public DuplicateTransferItemException(java.util.UUID productId) {
        super("Product " + productId + " appears more than once in the transfer");
    }
}
