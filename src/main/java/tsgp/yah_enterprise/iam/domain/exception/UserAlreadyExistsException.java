package tsgp.yah_enterprise.iam.domain.exception;

import tsgp.yah_enterprise.iam.domain.valueobject.Username;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(Username username) {
        super("A user with username: " + username.value() + " already exists");
    }
}