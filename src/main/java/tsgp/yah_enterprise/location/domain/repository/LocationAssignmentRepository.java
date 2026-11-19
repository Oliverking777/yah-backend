package tsgp.yah_enterprise.location.domain.repository;

import tsgp.yah_enterprise.location.domain.model.LocationAssignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationAssignmentRepository {
    LocationAssignment save(LocationAssignment assignment);
    Optional<LocationAssignment> findByUserId(UUID userId);
    List<LocationAssignment> findAllByLocationId(UUID locationId);
    boolean existsByUserId(UUID userId);
    void delete(LocationAssignment assignment);
}