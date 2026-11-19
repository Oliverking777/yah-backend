package tsgp.yah_enterprise.payment.domain.exception;

/** 404: the record does not exist. */
public class ReceivableNotFoundException extends PaymentDomainException {
    public ReceivableNotFoundException(String message) {
        super(message);
    }
}
