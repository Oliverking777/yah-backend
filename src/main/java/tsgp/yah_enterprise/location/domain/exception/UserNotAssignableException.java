package tsgp.yah_enterprise.location.domain.exception;

import java.util.UUID;

public class UserNotAssignableException extends LocationDomainException {
    public UserNotAssignableException(UUID userId, String reason) {
        super("User " + userId + " cannot be assigned to a location: " + reason);
    }
}