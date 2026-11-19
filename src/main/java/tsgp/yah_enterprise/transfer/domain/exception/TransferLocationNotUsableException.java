package tsgp.yah_enterprise.transfer.domain.exception;

/** 409: a location is missing or inactive. */
public class TransferLocationNotUsableException extends TransferDomainException {
    public TransferLocationNotUsableException(String message) {
        super(message);
    }
}
