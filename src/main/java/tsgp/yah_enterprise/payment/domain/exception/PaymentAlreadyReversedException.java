package tsgp.yah_enterprise.payment.domain.exception;

/** 409: conflicts with the current state of the data. */
public class PaymentAlreadyReversedException extends PaymentDomainException {
    public PaymentAlreadyReversedException(String message) {
        super(message);
    }
}
