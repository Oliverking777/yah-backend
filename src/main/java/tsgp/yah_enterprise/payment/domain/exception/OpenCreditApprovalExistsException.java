package tsgp.yah_enterprise.payment.domain.exception;

/** 409: conflicts with the current state of the data. */
public class OpenCreditApprovalExistsException extends PaymentDomainException {
    public OpenCreditApprovalExistsException(String message) {
        super(message);
    }
}
