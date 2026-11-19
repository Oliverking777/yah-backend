package tsgp.yah_enterprise.iam.domain.exception;

import java.util.Set;
import java.util.UUID;

import tsgp.yah_enterprise.iam.domain.valueobject.PermissionCode;

public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException(UUID permissionId) {
        super("Permission with id: " + permissionId + " was not found");
    }

    public PermissionNotFoundException(Set<UUID> permissionIds) {
        super("Permissions with ids: " + permissionIds + " were not found");
    }

    public PermissionNotFoundException(PermissionCode permissionCode) {
        super("Permission with code: " + permissionCode.value() + " was not found");
    }
}