package tsgp.yah_enterprise.location.domain.exception;

import java.util.UUID;

public class LocationAlreadyInactiveException extends LocationDomainException {
    public LocationAlreadyInactiveException(UUID locationId) {
        super("Location is already inactive: " + locationId);
    }
}