package tsgp.yah_enterprise.location.domain.exception;

import java.util.UUID;

public class LocationNotFoundException extends LocationDomainException {
    public LocationNotFoundException(UUID locationId) {
        super("Location not found: " + locationId);
    }
}