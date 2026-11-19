package tsgp.yah_enterprise.location.application.port.in;

import tsgp.yah_enterprise.location.application.dto.LocationResult;

import java.util.UUID;

public interface GetLocationUseCase {

    /**
     * @throws tsgp.yah_enterprise.location.domain.exception.LocationNotFoundException if the location does not exist
     */
    LocationResult getLocationById(UUID locationId);
}