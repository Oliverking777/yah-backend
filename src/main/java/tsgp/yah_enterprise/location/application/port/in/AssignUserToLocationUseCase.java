package tsgp.yah_enterprise.location.application.port.in;

import tsgp.yah_enterprise.location.application.dto.AssignUserToLocationCommand;
import tsgp.yah_enterprise.location.application.dto.LocationUserResult;

import java.util.UUID;

public interface AssignUserToLocationUseCase {

    /**
     * Assigns a user to a location. A user belongs to at most one location.
     *
     * @throws tsgp.yah_enterprise.location.domain.exception.LocationNotFoundException      if the location does not exist
     * @throws tsgp.yah_enterprise.location.domain.exception.LocationInactiveException      if the location is inactive
     * @throws tsgp.yah_enterprise.location.domain.exception.LocationUserNotFoundException  if the user does not exist
     * @throws tsgp.yah_enterprise.location.domain.exception.UserNotAssignableException     if the user is inactive
     * @throws tsgp.yah_enterprise.location.domain.exception.UserAlreadyAssignedException   if the user already has a location
     */
    LocationUserResult assignUser(UUID locationId, AssignUserToLocationCommand command);
}