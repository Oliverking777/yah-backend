package tsgp.yah_enterprise.location.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tsgp.yah_enterprise.location.infrastructure.persistence.entity.LocationJpaEntity;
import tsgp.yah_enterprise.location.domain.model.LocationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationJpaRepository extends JpaRepository<LocationJpaEntity, UUID> {

    Optional<LocationJpaEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<LocationJpaEntity> findAllByOrderByNameAsc();

    Page<LocationJpaEntity> findAllByOrderByNameAsc(Pageable pageable);

    List<LocationJpaEntity> findAllByLocationStatusOrderByNameAsc(LocationStatus locationStatus);
}