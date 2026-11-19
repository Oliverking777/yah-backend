package tsgp.yah_enterprise.iam.domain.exception;

/**
 * Thrown when a first name or last name is invalid.
 */
public class InvalidPersonNameException extends RuntimeException {

    public InvalidPersonNameException(String field, String reason) {
        super("The " + field + " is invalid. " + reason);
    }
}