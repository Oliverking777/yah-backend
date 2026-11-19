package tsgp.yah_enterprise.payment.domain.exception;

/** 404: the customer does not exist. */
public class PaymentCustomerNotFoundException extends PaymentDomainException {
    public PaymentCustomerNotFoundException(String message) {
        super(message);
    }
}
