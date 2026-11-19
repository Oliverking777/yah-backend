package tsgp.yah_enterprise.unit.domain.exception;

/** Base type for all Unit-module business rule violations. */
public abstract class UnitDomainException extends RuntimeException {
    protected UnitDomainException(String message) {
        super(message);
    }
}
