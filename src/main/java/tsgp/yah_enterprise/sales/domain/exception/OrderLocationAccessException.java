package tsgp.yah_enterprise.sales.domain.exception;

/** 403: the user is not assigned to the location the action needs. */
public class OrderLocationAccessException extends SalesDomainException {
    public OrderLocationAccessException(String message) {
        super(message);
    }
}
