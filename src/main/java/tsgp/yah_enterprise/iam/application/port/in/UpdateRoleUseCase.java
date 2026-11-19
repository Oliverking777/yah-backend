package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.iam.application.dto.RoleResult;
import tsgp.yah_enterprise.iam.application.dto.UpdateRoleCommand;

public interface UpdateRoleUseCase {

    /**
     * Updates the name and/or description of an existing role.
     * A null field in the request means "leave unchanged".
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException           if no role has this id
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidRoleNameException        if the new name is invalid
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidRoleDescriptionException if the new description is invalid
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleAlreadyExistsException      if the new name belongs to another role
     */
    RoleResult updateRole(UUID roleId, UpdateRoleCommand request);
}