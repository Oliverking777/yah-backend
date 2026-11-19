package tsgp.yah_enterprise.iam.domain.exception;

/**
 * Thrown when someone tries to rename, delete, or change the location scope of a built-in (system) role.
 * NOTE: extend the same base class as your other IAM domain exceptions if they have one.
 */
public class SystemRoleProtectedException extends RuntimeException {

    public SystemRoleProtectedException(String roleName, String action) {
        super("The system role " + roleName + " cannot " + action + ".");
    }
}