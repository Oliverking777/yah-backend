package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.iam.application.dto.BulkRolePermissionsCommand;
import tsgp.yah_enterprise.iam.application.dto.RolePermissionsResult;

public interface BulkAddRolePermissionsUseCase {

    /**
     * Adds several permissions to a role and keeps the ones it already has.
     * Ids the role already has are ignored. All or nothing: if any id does not exist, nothing is added.
     *
     * @return the role with its complete list of permissions after the change
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException       if no role has this id
     * @throws tsgp.yah_enterprise.iam.domain.exception.PermissionNotFoundException if any permission id does not exist
     */
    RolePermissionsResult addRolePermissions(UUID roleId, BulkRolePermissionsCommand command);
}