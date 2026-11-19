package tsgp.yah_enterprise.iam.application.port.in;

import tsgp.yah_enterprise.iam.application.dto.InitializeFirstUsersCommand;
import tsgp.yah_enterprise.iam.application.dto.InitializeFirstUsersResult;

public interface InitializeFirstUsersUseCase {

    /**
     * Creates the first ADMIN and OWNER users on an empty system. Safe to run on every start:
     * it does nothing if any user already exists. A user is only created if its password is given.
     * The ADMIN and OWNER roles come from the Flyway migration V10.
     * Everything happens in one transaction: if one user fails, neither is kept.
     *
     * @throws IllegalStateException                                               if the ADMIN or OWNER role does not exist
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidPasswordException   if a password breaks the rules
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidPersonNameException if a name is invalid
     */
    InitializeFirstUsersResult initialize(InitializeFirstUsersCommand command);
}