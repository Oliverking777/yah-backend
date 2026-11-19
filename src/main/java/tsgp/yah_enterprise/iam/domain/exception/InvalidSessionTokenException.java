package tsgp.yah_enterprise.iam.domain.exception;

/**
 * Thrown when a session token does not have the format of a token we issue.
 * The message never contains the token itself.
 */
public class InvalidSessionTokenException extends RuntimeException {

    public InvalidSessionTokenException() {
        super("The session token is invalid");
    }

    public InvalidSessionTokenException(String reason) {
        super(reason);
    }
}