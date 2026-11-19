package tsgp.yah_enterprise.sales.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tsgp.yah_enterprise.sales.infrastructure.persistence.entity.ReceiptJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptJpaRepository extends JpaRepository<ReceiptJpaEntity, UUID> {

    Optional<ReceiptJpaEntity> findByOrderId(UUID orderId);
}
