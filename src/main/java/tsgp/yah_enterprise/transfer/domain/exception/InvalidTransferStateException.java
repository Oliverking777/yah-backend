package tsgp.yah_enterprise.transfer.domain.exception;

/** 409: the move is not allowed from the current status. */
public class InvalidTransferStateException extends TransferDomainException {
    public InvalidTransferStateException(String message) {
        super(message);
    }
}
