package tsgp.yah_enterprise.payment.domain.exception;

/** 400: the request is not valid. */
public class InvalidPaymentException extends PaymentDomainException {
    public InvalidPaymentException(String message) {
        super(message);
    }
}
