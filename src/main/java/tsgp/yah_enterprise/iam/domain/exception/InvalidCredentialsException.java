package tsgp.yah_enterprise.iam.domain.exception;

/**
 * Thrown when a login fails. The message is deliberately the same whether the username
 * or the password was wrong, so it does not reveal which usernames exist.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}