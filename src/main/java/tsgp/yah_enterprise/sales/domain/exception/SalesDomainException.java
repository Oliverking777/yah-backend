package tsgp.yah_enterprise.sales.domain.exception;

/** Base of every business-rule error of the Sales module. */
public abstract class SalesDomainException extends RuntimeException {
    protected SalesDomainException(String message) {
        super(message);
    }
}
