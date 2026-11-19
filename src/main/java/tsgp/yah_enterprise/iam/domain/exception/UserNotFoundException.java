package tsgp.yah_enterprise.iam.domain.exception;

import java.util.UUID;

import tsgp.yah_enterprise.iam.domain.valueobject.Username;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("User with id: " + userId + " was not found");
    }

    public UserNotFoundException(Username username) {
        super("User with username: " + username.value() + " was not found");
    }
}