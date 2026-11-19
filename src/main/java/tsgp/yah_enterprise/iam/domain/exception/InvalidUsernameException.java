package tsgp.yah_enterprise.iam.domain.exception;

public class InvalidUsernameException extends RuntimeException {

    public InvalidUsernameException(String username) {
        super("The username: " + username + " is invalid");
    }

    public InvalidUsernameException(String username, String reason) {
        super("The username: " + username + " is invalid. " + reason);
    }
}