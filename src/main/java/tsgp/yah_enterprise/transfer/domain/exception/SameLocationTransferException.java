package tsgp.yah_enterprise.transfer.domain.exception;

/** 400: source and destination are the same. */
public class SameLocationTransferException extends TransferDomainException {
    public SameLocationTransferException() {
        super("Source and destination location must be different");
    }
}
