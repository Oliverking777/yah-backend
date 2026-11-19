package tsgp.yah_enterprise.iam.domain.exception;

public class InvalidPermissionCodeException extends RuntimeException {

    public InvalidPermissionCodeException(String code) {
        super("The code for this permission: " + code + " is invalid");
    }

    public InvalidPermissionCodeException(String code, String reason) {
        super("The code for this permission: " + code + " is invalid. " + reason);
    }
}