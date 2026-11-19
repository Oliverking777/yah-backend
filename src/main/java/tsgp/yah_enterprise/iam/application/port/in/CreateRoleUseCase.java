package tsgp.yah_enterprise.iam.application.port.in;

import tsgp.yah_enterprise.iam.application.dto.CreateRoleCommand;
import tsgp.yah_enterprise.iam.application.dto.RoleResult;

public interface CreateRoleUseCase {

    /**
     * Creates a new role.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidRoleNameException        if the name is invalid
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidRoleDescriptionException if the description is invalid
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleAlreadyExistsException      if the name is already taken
     */
    RoleResult createRole(CreateRoleCommand request);
}