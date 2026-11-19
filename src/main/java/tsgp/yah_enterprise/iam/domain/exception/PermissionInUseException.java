package tsgp.yah_enterprise.iam.domain.exception;

import java.util.UUID;

public class PermissionInUseException extends RuntimeException {

    public PermissionInUseException(UUID permissionId) {
        super("Permission with id: " + permissionId + " is still assigned to one or more roles");
    }
}