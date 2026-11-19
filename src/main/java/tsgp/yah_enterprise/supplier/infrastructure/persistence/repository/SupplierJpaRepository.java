package tsgp.yah_enterprise.supplier.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tsgp.yah_enterprise.supplier.domain.model.SupplierStatus;
import tsgp.yah_enterprise.supplier.infrastructure.persistence.entity.SupplierJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierJpaRepository extends JpaRepository<SupplierJpaEntity, UUID> {

    Optional<SupplierJpaEntity> findBySupplierCode(String supplierCode);

    List<SupplierJpaEntity> findAllBySupplierIdIn(Iterable<UUID> ids);

    boolean existsBySupplierNameIgnoreCase(String supplierName);

    Page<SupplierJpaEntity> findAllBySupplierStatus(SupplierStatus status, Pageable pageable);

    long countBySupplierStatus(SupplierStatus status);

    @Query("""
            SELECT s FROM SupplierJpaEntity s
            WHERE LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :term, '%'))
               OR LOWER(s.supplierCode) LIKE LOWER(CONCAT('%', :term, '%'))
               OR s.phoneNumber LIKE CONCAT('%', :term, '%')
            """)
    Page<SupplierJpaEntity> search(@Param("term") String term, Pageable pageable);
}
