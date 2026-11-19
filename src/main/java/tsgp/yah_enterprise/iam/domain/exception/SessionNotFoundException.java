package tsgp.yah_enterprise.iam.domain.exception;

/**
 * Thrown when no session matches a token (it never existed, was logged out, or was replaced by a newer login).
 * The message is deliberately vague and never contains the token.
 */
public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException() {
        super("No active session found");
    }
}