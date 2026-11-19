package tsgp.yah_enterprise.location.domain.exception;

import java.util.UUID;

public class LocationInactiveException extends LocationDomainException {
    public LocationInactiveException(UUID locationId) {
        super("Location is inactive: " + locationId);
    }
}