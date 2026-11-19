package tsgp.yah_enterprise.iam.domain.exception;

public class InvalidRoleNameException extends RuntimeException {

    public InvalidRoleNameException(String name) {
        super("The name for this role: " + name + " is invalid");
    }

    public InvalidRoleNameException(String name, String reason) {
        super("The name for this role: " + name + " is invalid. " + reason);
    }
}