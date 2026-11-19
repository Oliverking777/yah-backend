package tsgp.yah_enterprise.iam.application.port.in;

import tsgp.yah_enterprise.iam.application.dto.CreateUserCommand;
import tsgp.yah_enterprise.iam.application.dto.UserResult;

public interface CreateUserUseCase {

    /**
     * Creates a user with a generated username and a hashed password.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidPersonNameException if a name is invalid
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidPasswordException   if the password breaks the rules
     * @throws tsgp.yah_enterprise.iam.domain.exception.RoleNotFoundException      if the role does not exist
     */
    UserResult createUser(CreateUserCommand command);
}