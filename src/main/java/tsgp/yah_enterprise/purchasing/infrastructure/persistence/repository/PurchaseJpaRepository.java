package tsgp.yah_enterprise.purchasing.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tsgp.yah_enterprise.purchasing.infrastructure.persistence.entity.PurchaseJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseJpaRepository extends JpaRepository<PurchaseJpaEntity, UUID>, JpaSpecificationExecutor<PurchaseJpaEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PurchaseJpaEntity p WHERE p.purchaseId = :id")
    Optional<PurchaseJpaEntity> findForUpdate(@Param("id") UUID id);

    Optional<PurchaseJpaEntity> findByPurchaseNumber(String purchaseNumber);
}
