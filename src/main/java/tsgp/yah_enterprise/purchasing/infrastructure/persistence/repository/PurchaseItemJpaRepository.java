package tsgp.yah_enterprise.purchasing.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tsgp.yah_enterprise.purchasing.infrastructure.persistence.entity.PurchaseItemJpaEntity;

import java.util.UUID;

public interface PurchaseItemJpaRepository extends JpaRepository<PurchaseItemJpaEntity, UUID> {

    @Query(value = "SELECT DISTINCT i.productId FROM PurchaseItemJpaEntity i WHERE i.purchase.supplierId = :supplierId",
           countQuery = "SELECT COUNT(DISTINCT i.productId) FROM PurchaseItemJpaEntity i WHERE i.purchase.supplierId = :supplierId")
    Page<UUID> findDistinctProductIdsBySupplier(@Param("supplierId") UUID supplierId, Pageable pageable);
}
