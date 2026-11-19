package tsgp.yah_enterprise.location.domain.exception;

/** Base type for all Location-module business rule violations. */
public abstract class LocationDomainException extends RuntimeException {
    protected LocationDomainException(String message) {
        super(message);
    }
}