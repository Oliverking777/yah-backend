package tsgp.yah_enterprise.location.application.port.in;

import java.util.UUID;

public interface RemoveUserFromLocationUseCase {

    /**
     * Removes a user's assignment to a location.
     *
     * @throws tsgp.yah_enterprise.location.domain.exception.LocationNotFoundException   if the location does not exist
     * @throws tsgp.yah_enterprise.location.domain.exception.UserNotAssignedException    if the user is not assigned to this location
     */
    void removeUser(UUID locationId, UUID userId);
}