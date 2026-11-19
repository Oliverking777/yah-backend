package tsgp.yah_enterprise.location.domain.exception;

import java.util.UUID;

public class UserNotAssignedException extends LocationDomainException {
    public UserNotAssignedException(UUID userId, UUID locationId) {
        super("User " + userId + " is not assigned to location " + locationId);
    }
}