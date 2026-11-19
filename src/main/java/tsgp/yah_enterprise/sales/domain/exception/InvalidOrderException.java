package tsgp.yah_enterprise.sales.domain.exception;

/** 400: the request is not valid. */
public class InvalidOrderException extends SalesDomainException {
    public InvalidOrderException(String message) {
        super(message);
    }
}
