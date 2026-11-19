package tsgp.yah_enterprise.iam.domain.exception;

import tsgp.yah_enterprise.iam.domain.valueobject.PermissionCode;

public class PermissionAlreadyExistsException extends RuntimeException {

    public PermissionAlreadyExistsException(PermissionCode permissionCode) {
        super("A permission with code: " + permissionCode.value() + " already exists");
    }
}