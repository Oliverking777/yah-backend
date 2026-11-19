package tsgp.yah_enterprise.location.domain.exception;

import java.util.UUID;

public class LocationAlreadyActiveException extends LocationDomainException {
    public LocationAlreadyActiveException(UUID locationId) {
        super("Location is already active: " + locationId);
    }
}