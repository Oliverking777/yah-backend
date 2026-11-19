package tsgp.yah_enterprise.location.application.port.in;

import tsgp.yah_enterprise.location.application.dto.LocationResult;

import java.util.Optional;
import java.util.UUID;

/**
 * Entry point for other modules (Inventory, Sales...) that need to know
 * where a user works, for example to limit an employee to their own location.
 */
public interface GetUserLocationUseCase {

    /**
     * Returns the location a user is assigned to, or empty if the user has none.
     */
    Optional<LocationResult> findLocationOfUser(UUID userId);
}