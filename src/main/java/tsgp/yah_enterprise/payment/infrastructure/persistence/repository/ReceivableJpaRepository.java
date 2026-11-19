package tsgp.yah_enterprise.payment.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tsgp.yah_enterprise.payment.infrastructure.persistence.entity.ReceivableJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceivableJpaRepository
        extends JpaRepository<ReceivableJpaEntity, UUID>, JpaSpecificationExecutor<ReceivableJpaEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReceivableJpaEntity r WHERE r.saleId = :id")
    Optional<ReceivableJpaEntity> findForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReceivableJpaEntity r WHERE r.customerId = :customerId "
            + "AND r.totalAmount > r.allocatedAmount ORDER BY r.createdAt ASC")
    List<ReceivableJpaEntity> findOpenForUpdate(@Param("customerId") UUID customerId);

    @Query("SELECT COALESCE(SUM(r.totalAmount - r.allocatedAmount), 0) FROM ReceivableJpaEntity r "
            + "WHERE r.customerId = :customerId")
    long sumOutstanding(@Param("customerId") UUID customerId);
}
