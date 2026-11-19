package tsgp.yah_enterprise.location.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tsgp.yah_enterprise.location.infrastructure.persistence.entity.LocationAssignmentJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationAssignmentJpaRepository extends JpaRepository<LocationAssignmentJpaEntity, UUID> {

    Optional<LocationAssignmentJpaEntity> findByUserId(UUID userId);

    List<LocationAssignmentJpaEntity> findAllByLocationIdOrderByAssignedAtAsc(UUID locationId);

    boolean existsByUserId(UUID userId);
}