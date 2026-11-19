package tsgp.yah_enterprise.location.application.port.in;

import tsgp.yah_enterprise.location.application.dto.LocationResult;

import java.util.UUID;

public interface ChangeLocationStatusUseCase {

    /**
     * Makes a location operational.
     *
     * @throws tsgp.yah_enterprise.location.domain.exception.LocationNotFoundException       if the location does not exist
     * @throws tsgp.yah_enterprise.location.domain.exception.LocationAlreadyActiveException  if it is already active
     */
    LocationResult activateLocation(UUID locationId);

    /**
     * Temporarily disables a location. Existing assignments are kept.
     *
     * @throws tsgp.yah_enterprise.location.domain.exception.LocationNotFoundException         if the location does not exist
     * @throws tsgp.yah_enterprise.location.domain.exception.LocationAlreadyInactiveException  if it is already inactive
     */
    LocationResult deactivateLocation(UUID locationId);
}