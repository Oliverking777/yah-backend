package tsgp.yah_enterprise.iam.domain.exception;

import java.util.UUID;

import tsgp.yah_enterprise.iam.domain.valueobject.RoleName;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(UUID roleId) {
        super("Role with id: " + roleId + " was not found");
    }

    public RoleNotFoundException(RoleName roleName) {
        super("Role with name: " + roleName.value() + " was not found");
    }
}