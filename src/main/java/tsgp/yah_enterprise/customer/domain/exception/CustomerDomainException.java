package tsgp.yah_enterprise.customer.domain.exception;

public abstract class CustomerDomainException extends RuntimeException {
    protected CustomerDomainException(String message) {
        super(message);
    }
}
