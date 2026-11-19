package tsgp.yah_enterprise.payment.domain.exception;

/** 409: conflicts with the current state of the data. */
public class SaleAlreadySettledException extends PaymentDomainException {
    public SaleAlreadySettledException(String message) {
        super(message);
    }
}
