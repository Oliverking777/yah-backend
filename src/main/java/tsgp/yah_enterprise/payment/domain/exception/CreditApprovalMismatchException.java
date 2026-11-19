package tsgp.yah_enterprise.payment.domain.exception;

/** 409: conflicts with the current state of the data. */
public class CreditApprovalMismatchException extends PaymentDomainException {
    public CreditApprovalMismatchException(String message) {
        super(message);
    }
}
