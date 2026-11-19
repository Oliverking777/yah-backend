package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.iam.application.dto.RoleResult;

public interface GetRoleUseCase {

    /**
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException if no role has this id
     */
    RoleResult getRoleById(UUID roleId);

    /**
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidRoleNameException if the name is invalid
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException    if no role has this name
     */
    RoleResult getRoleByName(String roleName);
}