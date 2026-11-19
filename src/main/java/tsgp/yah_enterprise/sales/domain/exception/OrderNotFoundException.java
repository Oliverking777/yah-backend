package tsgp.yah_enterprise.sales.domain.exception;

/** 404: the order does not exist. */
public class OrderNotFoundException extends SalesDomainException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
