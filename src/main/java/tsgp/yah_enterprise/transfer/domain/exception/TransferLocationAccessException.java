package tsgp.yah_enterprise.transfer.domain.exception;

/** 403: the user is not assigned to the location the action needs. */
public class TransferLocationAccessException extends TransferDomainException {
    public TransferLocationAccessException(String message) {
        super(message);
    }
}
