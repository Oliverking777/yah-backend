package tsgp.yah_enterprise.iam.domain.exception;

public class InvalidRoleDescriptionException extends RuntimeException {

    public InvalidRoleDescriptionException(String reason) {
        super("The description for this role is invalid. " + reason);
    }
}