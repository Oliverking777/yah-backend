package tsgp.yah_enterprise.sales.domain.exception;

/** 400: the request is not valid. */
public class InvalidDiscountException extends SalesDomainException {
    public InvalidDiscountException(String message) {
        super(message);
    }
}
