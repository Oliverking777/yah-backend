package tsgp.yah_enterprise.payment.domain.exception;

/** 409: conflicts with the current state of the data. */
public class AllocationCustomerMismatchException extends PaymentDomainException {
    public AllocationCustomerMismatchException(String message) {
        super(message);
    }
}
