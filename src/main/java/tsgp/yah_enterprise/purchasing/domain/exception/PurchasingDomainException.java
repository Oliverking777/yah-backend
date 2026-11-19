package tsgp.yah_enterprise.purchasing.domain.exception;

/** Base of every Purchasing business-rule failure. The presentation layer maps subclasses to HTTP statuses. */
public abstract class PurchasingDomainException extends RuntimeException {
    protected PurchasingDomainException(String message) {
        super(message);
    }
}
