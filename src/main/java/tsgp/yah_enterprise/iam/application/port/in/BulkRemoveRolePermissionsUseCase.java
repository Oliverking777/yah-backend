package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.iam.application.dto.BulkRolePermissionsCommand;
import tsgp.yah_enterprise.iam.application.dto.RolePermissionsResult;

public interface BulkRemoveRolePermissionsUseCase {

    /**
     * Removes several permissions from a role and keeps the rest.
     * Ids the role does not have are ignored.
     *
     * @return the role with its complete list of permissions after the change
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException if no role has this id
     */
    RolePermissionsResult removeRolePermissions(UUID roleId, BulkRolePermissionsCommand command);
}