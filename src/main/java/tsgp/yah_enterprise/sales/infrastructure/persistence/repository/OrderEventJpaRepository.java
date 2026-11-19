package tsgp.yah_enterprise.sales.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tsgp.yah_enterprise.sales.infrastructure.persistence.entity.OrderEventJpaEntity;

import java.util.List;
import java.util.UUID;

public interface OrderEventJpaRepository extends JpaRepository<OrderEventJpaEntity, UUID> {

    List<OrderEventJpaEntity> findAllByOrderIdOrderByOccurredAtAsc(UUID orderId);
}
