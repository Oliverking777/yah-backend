package tsgp.yah_enterprise.iam.application.port.in;

import java.util.UUID;

import tsgp.yah_enterprise.iam.application.dto.UpdateUserCommand;
import tsgp.yah_enterprise.iam.application.dto.UserResult;

public interface UpdateUserUseCase {

    /**
     * Updates the name and/or role of a user. A null field means "leave unchanged".
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.UserNotFoundException      if no user has this id
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidPersonNameException if a name is invalid
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException      if the new role does not exist
     */
    UserResult updateUser(UUID userId, UpdateUserCommand command);
}