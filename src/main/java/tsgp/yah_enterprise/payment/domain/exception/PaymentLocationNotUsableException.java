package tsgp.yah_enterprise.payment.domain.exception;

/** 409: conflicts with the current state of the data. */
public class PaymentLocationNotUsableException extends PaymentDomainException {
    public PaymentLocationNotUsableException(String message) {
        super(message);
    }
}
