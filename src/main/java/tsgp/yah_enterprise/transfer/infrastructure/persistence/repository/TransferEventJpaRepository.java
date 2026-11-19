package tsgp.yah_enterprise.transfer.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tsgp.yah_enterprise.transfer.infrastructure.persistence.entity.TransferEventJpaEntity;

import java.util.List;
import java.util.UUID;

public interface TransferEventJpaRepository extends JpaRepository<TransferEventJpaEntity, UUID> {

    List<TransferEventJpaEntity> findAllByTransferIdOrderByOccurredAtAsc(UUID transferId);
}
