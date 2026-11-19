package tsgp.yah_enterprise.unit.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tsgp.yah_enterprise.unit.domain.model.UnitStatus;
import tsgp.yah_enterprise.unit.infrastructure.persistence.entity.UnitJpaEntity;

public interface UnitJpaRepository extends JpaRepository<UnitJpaEntity, UUID> {

    Optional<UnitJpaEntity> findByUnitNameIgnoreCase(String unitName);

    Optional<UnitJpaEntity> findByUnitCodeIgnoreCase(String unitCode);

    boolean existsByUnitNameIgnoreCase(String unitName);

    boolean existsByUnitCodeIgnoreCase(String unitCode);

    Page<UnitJpaEntity> findAllByOrderByUnitNameAsc(Pageable pageable);

    Page<UnitJpaEntity> findAllByUnitStatusOrderByUnitNameAsc(UnitStatus unitStatus, Pageable pageable);

    long countByUnitStatus(UnitStatus unitStatus);
}
