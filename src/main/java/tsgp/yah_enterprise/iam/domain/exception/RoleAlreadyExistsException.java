package tsgp.yah_enterprise.iam.domain.exception;

import tsgp.yah_enterprise.iam.domain.valueobject.RoleName;

public class RoleAlreadyExistsException extends RuntimeException {

    public RoleAlreadyExistsException(RoleName roleName) {
        super("A role with name: " + roleName.value() + " already exists");
    }
}