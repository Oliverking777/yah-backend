package tsgp.yah_enterprise.transfer.domain.exception;

/** 400: the request is not valid. */
public class InvalidTransferException extends TransferDomainException {
    public InvalidTransferException(String message) {
        super(message);
    }
}
