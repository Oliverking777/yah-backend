package tsgp.yah_enterprise.iam.domain.exception;

import tsgp.yah_enterprise.iam.domain.valueobject.Username;

/**
 * Thrown when an inactive (deactivated) user tries to do something that needs an active account, e.g. log in.
 */
public class UserInactiveException extends RuntimeException {

    public UserInactiveException(Username username) {
        super("The user: " + username.value() + " is inactive");
    }
}