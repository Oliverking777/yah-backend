package tsgp.yah_enterprise.location.domain.exception;

import java.util.UUID;

public class LocationUserNotFoundException extends LocationDomainException {
    public LocationUserNotFoundException(UUID userId) {
        super("User not found: " + userId);
    }
}