package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

public interface DeleteRoleUseCase {

    /**
     * Deletes a role.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException if no role has this id
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleInUseException   if users still have this role
     */
    void deleteRole(UUID roleId);
}