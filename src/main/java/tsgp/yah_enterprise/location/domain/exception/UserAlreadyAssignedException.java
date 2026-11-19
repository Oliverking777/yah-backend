package tsgp.yah_enterprise.location.domain.exception;

import java.util.UUID;

public class UserAlreadyAssignedException extends LocationDomainException {
    public UserAlreadyAssignedException(UUID userId) {
        super("User is already assigned to a location: " + userId);
    }
}