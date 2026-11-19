package tsgp.yah_enterprise.location.application.port.in;

import tsgp.yah_enterprise.location.application.dto.LocationUserResult;

import java.util.List;
import java.util.UUID;

public interface ListLocationUsersUseCase {

    /**
     * Returns the users assigned to a location, oldest assignment first.
     * Users that no longer exist in IAM are left out.
     *
     * @throws tsgp.yah_enterprise.location.domain.exception.LocationNotFoundException if the location does not exist
     */
    List<LocationUserResult> listLocationUsers(UUID locationId);
}