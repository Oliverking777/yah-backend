package tsgp.yah_enterprise.sales.domain.exception;

/** 409: conflicts with the current state of the data. */
public class ReceiptNotAvailableException extends SalesDomainException {
    public ReceiptNotAvailableException(String message) {
        super(message);
    }
}
