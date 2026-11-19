package tsgp.yah_enterprise.iam.application.port.in;

import tsgp.yah_enterprise.iam.application.dto.LoginCommand;
import tsgp.yah_enterprise.iam.application.dto.LoginResult;

public interface LoginUseCase {

    /**
     * Logs a user in. Any previous session of the same user is ended, and a new one is started.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidCredentialsException if the username or password is wrong
     *         (the same error for both, and for unknown usernames)
     * @throws tsgp.yah_enterprise.iam.domain.exception.UserInactiveException       if the password is right
     *         but the account is deactivated
     */
    LoginResult login(LoginCommand command);
}