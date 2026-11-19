package tsgp.yah_enterprise.payment.domain.exception;

/** 409: conflicts with the current state of the data. */
public class ReceivableOverAllocationException extends PaymentDomainException {
    public ReceivableOverAllocationException(String message) {
        super(message);
    }
}
