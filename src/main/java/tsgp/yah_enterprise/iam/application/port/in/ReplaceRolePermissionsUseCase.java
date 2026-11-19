package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.iam.application.dto.ReplaceRolePermissionsCommand;
import tsgp.yah_enterprise.iam.application.dto.RolePermissionsResult;

public interface ReplaceRolePermissionsUseCase {

    /**
     * Sets the complete list of permissions of a role.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException       if no role has this id
     * @throws tsgp.yah_enterprise.iam.domain.exception.PermissionNotFoundException if any permission id does not exist
     */
    RolePermissionsResult replaceRolePermissions(UUID roleId, ReplaceRolePermissionsCommand command);
}