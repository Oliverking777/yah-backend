package tsgp.yah_enterprise.iam.domain.exception;

/**
 * Thrown when a user changing their own password gives a wrong current password.
 */
public class IncorrectCurrentPasswordException extends RuntimeException {

    public IncorrectCurrentPasswordException() {
        super("The current password is incorrect");
    }
}