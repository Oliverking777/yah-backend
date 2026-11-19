package tsgp.yah_enterprise.payment.domain.exception;

/** 409: conflicts with the current state of the data. */
public class PaymentCustomerNotUsableException extends PaymentDomainException {
    public PaymentCustomerNotUsableException(String message) {
        super(message);
    }
}
