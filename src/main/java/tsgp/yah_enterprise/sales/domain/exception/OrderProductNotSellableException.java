package tsgp.yah_enterprise.sales.domain.exception;

/** 409: conflicts with the current state of the data. */
public class OrderProductNotSellableException extends SalesDomainException {
    public OrderProductNotSellableException(String message) {
        super(message);
    }
}
