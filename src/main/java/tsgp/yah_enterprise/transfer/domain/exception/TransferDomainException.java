package tsgp.yah_enterprise.transfer.domain.exception;

/** Base of every business-rule error of the Transfer module. */
public abstract class TransferDomainException extends RuntimeException {
    protected TransferDomainException(String message) {
        super(message);
    }
}
