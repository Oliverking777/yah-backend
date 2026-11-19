package tsgp.yah_enterprise.category.domain.exception;

/** Base type for all Category-module business rule violations. */
public abstract class CategoryDomainException extends RuntimeException {
    protected CategoryDomainException(String message) {
        super(message);
    }
}