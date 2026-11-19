package tsgp.yah_enterprise.sales.domain.exception;

/** 409: conflicts with the current state of the data. */
public class InvalidOrderStateException extends SalesDomainException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
