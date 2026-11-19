package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.iam.application.dto.RolePermissionsResult;

public interface GetRolePermissionsUseCase {

    /**
     * Lists the permissions granted by a role.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException if no role has this id
     */
    RolePermissionsResult getRolePermissions(UUID roleId);
}