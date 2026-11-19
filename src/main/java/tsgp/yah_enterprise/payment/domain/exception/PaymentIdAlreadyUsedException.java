package tsgp.yah_enterprise.payment.domain.exception;

/** 409: conflicts with the current state of the data. */
public class PaymentIdAlreadyUsedException extends PaymentDomainException {
    public PaymentIdAlreadyUsedException(String message) {
        super(message);
    }
}
