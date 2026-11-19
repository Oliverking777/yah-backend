package tsgp.yah_enterprise.sales.domain.exception;

/** 409: conflicts with the current state of the data. */
public class OrderLocationNotUsableException extends SalesDomainException {
    public OrderLocationNotUsableException(String message) {
        super(message);
    }
}
