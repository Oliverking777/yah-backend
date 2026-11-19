package tsgp.yah_enterprise.sales.domain.exception;

/** 409: conflicts with the current state of the data. */
public class DuplicateOrderItemException extends SalesDomainException {
    public DuplicateOrderItemException(String message) {
        super(message);
    }
}
