package tsgp.yah_enterprise.location.application.port.in;

import tsgp.yah_enterprise.location.application.dto.LocationResult;
import tsgp.yah_enterprise.location.application.dto.UpdateLocationCommand;

import java.util.UUID;

public interface UpdateLocationUseCase {

    /**
     * Updates the name, type and/or address of a location. Fields left null are unchanged.
     * The code and the status are not changed here.
     *
     * @throws tsgp.yah_enterprise.location.domain.exception.LocationNotFoundException     if the location does not exist
     * @throws tsgp.yah_enterprise.location.domain.exception.InvalidLocationException      if a value is invalid
     * @throws tsgp.yah_enterprise.location.domain.exception.InvalidLocationTypeException  if the type is not WAREHOUSE or STORE
     */
    LocationResult updateLocation(UUID locationId, UpdateLocationCommand command);
}