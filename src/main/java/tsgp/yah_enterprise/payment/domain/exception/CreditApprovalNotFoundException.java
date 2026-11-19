package tsgp.yah_enterprise.payment.domain.exception;

/** 404: the record does not exist. */
public class CreditApprovalNotFoundException extends PaymentDomainException {
    public CreditApprovalNotFoundException(String message) {
        super(message);
    }
}
