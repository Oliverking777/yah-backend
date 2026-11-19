package tsgp.yah_enterprise.location.domain.repository;

import tsgp.yah_enterprise.location.domain.model.Location;
import tsgp.yah_enterprise.location.domain.model.LocationCode;
import tsgp.yah_enterprise.location.domain.model.LocationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository {
    Location save(Location location);
    Optional<Location> findById(UUID locationId);
    Optional<Location> findByCode(LocationCode code);
    boolean existsByCode(LocationCode code);
    /** Returns every location, ordered by name. */
    List<Location> findAll();

    /**
     * Returns one page of locations, ordered by name.
     *
     * @param page zero-based page index
     * @param size number of locations per page
     */
    List<Location> findAll(int page, int size);

    /** Returns the total number of locations. */
    long count();
    List<Location> findAllByStatus(LocationStatus status);
}