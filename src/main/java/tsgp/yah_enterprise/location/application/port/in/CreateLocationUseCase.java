package tsgp.yah_enterprise.location.application.port.in;

import tsgp.yah_enterprise.location.application.dto.CreateLocationCommand;
import tsgp.yah_enterprise.location.application.dto.LocationResult;

public interface CreateLocationUseCase {

    /**
     * Creates a new, active location.
     *
     * @throws tsgp.yah_enterprise.location.domain.exception.InvalidLocationException            if the name, code or address is invalid
     * @throws tsgp.yah_enterprise.location.domain.exception.InvalidLocationTypeException        if the type is not WAREHOUSE or STORE
     * @throws tsgp.yah_enterprise.location.domain.exception.LocationCodeAlreadyExistsException  if the code is already taken
     */
    LocationResult createLocation(CreateLocationCommand command);
}