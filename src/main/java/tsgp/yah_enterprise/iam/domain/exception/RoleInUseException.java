package tsgp.yah_enterprise.iam.domain.exception;

import java.util.UUID;

public class RoleInUseException extends RuntimeException {

    public RoleInUseException(UUID roleId) {
        super("Role with id: " + roleId + " is still assigned to one or more users");
    }
}