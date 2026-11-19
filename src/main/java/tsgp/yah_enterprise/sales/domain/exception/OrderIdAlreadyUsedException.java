package tsgp.yah_enterprise.sales.domain.exception;

/** 409: conflicts with the current state of the data. */
public class OrderIdAlreadyUsedException extends SalesDomainException {
    public OrderIdAlreadyUsedException(String message) {
        super(message);
    }
}
