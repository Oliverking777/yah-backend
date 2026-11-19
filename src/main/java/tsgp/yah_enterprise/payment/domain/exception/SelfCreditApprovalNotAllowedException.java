package tsgp.yah_enterprise.payment.domain.exception;

/** 409: conflicts with the current state of the data. */
public class SelfCreditApprovalNotAllowedException extends PaymentDomainException {
    public SelfCreditApprovalNotAllowedException(String message) {
        super(message);
    }
}
