package tsgp.yah_enterprise.payment.domain.exception;

/** 403: the user is not assigned to the location the action needs. */
public class PaymentLocationAccessException extends PaymentDomainException {
    public PaymentLocationAccessException(String message) {
        super(message);
    }
}
