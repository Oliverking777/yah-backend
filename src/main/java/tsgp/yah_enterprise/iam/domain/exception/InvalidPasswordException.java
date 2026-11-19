package tsgp.yah_enterprise.iam.domain.exception;

/**
 * Thrown when a password breaks the password rules, or a password hash is malformed.
 * The message never contains the password itself.
 */
public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException(String reason) {
        super(reason);
    }
}