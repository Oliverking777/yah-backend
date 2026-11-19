package tsgp.yah_enterprise.payment.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tsgp.yah_enterprise.payment.domain.model.CreditApprovalStatus;
import tsgp.yah_enterprise.payment.infrastructure.persistence.entity.CreditApprovalJpaEntity;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface CreditApprovalJpaRepository
        extends JpaRepository<CreditApprovalJpaEntity, UUID>, JpaSpecificationExecutor<CreditApprovalJpaEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CreditApprovalJpaEntity c WHERE c.creditApprovalId = :id")
    Optional<CreditApprovalJpaEntity> findForUpdate(@Param("id") UUID id);

    Optional<CreditApprovalJpaEntity> findBySaleIdAndApprovalStatusIn(UUID saleId, Collection<CreditApprovalStatus> statuses);
}
