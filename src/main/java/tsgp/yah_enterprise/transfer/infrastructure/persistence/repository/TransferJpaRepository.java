package tsgp.yah_enterprise.transfer.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tsgp.yah_enterprise.transfer.infrastructure.persistence.entity.TransferJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface TransferJpaRepository
        extends JpaRepository<TransferJpaEntity, UUID>, JpaSpecificationExecutor<TransferJpaEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TransferJpaEntity t WHERE t.transferId = :id")
    Optional<TransferJpaEntity> findForUpdate(@Param("id") UUID id);

    Optional<TransferJpaEntity> findByTransferNumber(String transferNumber);
}
