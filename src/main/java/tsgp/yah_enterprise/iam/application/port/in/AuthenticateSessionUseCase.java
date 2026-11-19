package tsgp.yah_enterprise.iam.application.port.in;

import tsgp.yah_enterprise.iam.application.dto.AuthenticatedUser;

public interface AuthenticateSessionUseCase {

    /**
     * Turns a session token into the user it belongs to and their permissions.
     *
     * @throws tsgp.yah_enterprise.iam.domain.exception.InvalidSessionTokenException if the token is malformed
     * @throws tsgp.yah_enterprise.iam.domain.exception.SessionNotFoundException     if no session matches the token
     * @throws tsgp.yah_enterprise.iam.domain.exception.SessionExpiredException      if the session has expired
     * @throws tsgp.yah_enterprise.iam.domain.exception.UserInactiveException        if the user has been deactivated
     */
    AuthenticatedUser authenticate(String sessionToken);
}