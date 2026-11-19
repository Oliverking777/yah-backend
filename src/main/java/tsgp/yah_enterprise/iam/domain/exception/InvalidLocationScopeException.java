package tsgp.yah_enterprise.iam.domain.exception;

/**
 * Thrown when a location scope is missing or is not ALL or ASSIGNED.
 * NOTE: extend the same base class as your other IAM domain exceptions if they have one.
 */
public class InvalidLocationScopeException extends RuntimeException {

    public InvalidLocationScopeException(String value) {
        super("Invalid location scope: '" + value + "'. It must be ALL or ASSIGNED.");
    }
}