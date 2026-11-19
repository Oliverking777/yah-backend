package tsgp.yah_enterprise.payment.domain.exception;

/** 404: the record does not exist. */
public class PaymentNotFoundException extends PaymentDomainException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
