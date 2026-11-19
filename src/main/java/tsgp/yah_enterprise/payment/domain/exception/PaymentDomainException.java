package tsgp.yah_enterprise.payment.domain.exception;

/** Base of every business-rule error of the Payment module. */
public abstract class PaymentDomainException extends RuntimeException {
    protected PaymentDomainException(String message) {
        super(message);
    }
}
