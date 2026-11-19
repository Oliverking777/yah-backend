package tsgp.yah_enterprise.product.domain.exception;

public abstract class ProductDomainException extends RuntimeException {
    protected ProductDomainException(String message) {
        super(message);
    }
}
