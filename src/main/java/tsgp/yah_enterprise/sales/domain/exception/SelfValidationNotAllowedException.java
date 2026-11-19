package tsgp.yah_enterprise.sales.domain.exception;

/** 409: conflicts with the current state of the data. */
public class SelfValidationNotAllowedException extends SalesDomainException {
    public SelfValidationNotAllowedException(String message) {
        super(message);
    }
}
