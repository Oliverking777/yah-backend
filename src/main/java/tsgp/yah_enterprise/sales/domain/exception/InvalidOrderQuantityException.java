package tsgp.yah_enterprise.sales.domain.exception;

/** 400: the request is not valid. */
public class InvalidOrderQuantityException extends SalesDomainException {
    public InvalidOrderQuantityException(String message) {
        super(message);
    }
}
