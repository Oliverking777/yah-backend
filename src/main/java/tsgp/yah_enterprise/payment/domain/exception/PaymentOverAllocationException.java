package tsgp.yah_enterprise.payment.domain.exception;

/** 409: conflicts with the current state of the data. */
public class PaymentOverAllocationException extends PaymentDomainException {
    public PaymentOverAllocationException(String message) {
        super(message);
    }
}
