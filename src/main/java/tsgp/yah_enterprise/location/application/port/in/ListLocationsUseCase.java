package tsgp.yah_enterprise.location.application.port.in;

import tsgp.yah_enterprise.location.application.dto.LocationPageRequest;
import tsgp.yah_enterprise.location.application.dto.LocationPageResult;

public interface ListLocationsUseCase {

    /**
     * Returns one page of locations, ordered by name.
     */
    LocationPageResult listLocations(LocationPageRequest request);
}