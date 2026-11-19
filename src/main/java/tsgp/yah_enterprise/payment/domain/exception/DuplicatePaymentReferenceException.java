package tsgp.yah_enterprise.payment.domain.exception;

/** 409: conflicts with the current state of the data. */
public class DuplicatePaymentReferenceException extends PaymentDomainException {
    public DuplicatePaymentReferenceException(String message) {
        super(message);
    }
}
