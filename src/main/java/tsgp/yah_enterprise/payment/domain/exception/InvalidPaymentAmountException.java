package tsgp.yah_enterprise.payment.domain.exception;

/** 400: the request is not valid. */
public class InvalidPaymentAmountException extends PaymentDomainException {
    public InvalidPaymentAmountException(String message) {
        super(message);
    }
}
