package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

public interface RemoveRolePermissionUseCase {

    /**
     * Removes one permission from a role. Does nothing if the role does not have it.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException if no role has this id
     */
    void removeRolePermission(UUID roleId, UUID permissionId);
}