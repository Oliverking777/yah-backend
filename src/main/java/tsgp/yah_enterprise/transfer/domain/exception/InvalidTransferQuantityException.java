package tsgp.yah_enterprise.transfer.domain.exception;

/** 400: a quantity is not allowed. */
public class InvalidTransferQuantityException extends TransferDomainException {
    public InvalidTransferQuantityException(String message) {
        super(message);
    }
}
