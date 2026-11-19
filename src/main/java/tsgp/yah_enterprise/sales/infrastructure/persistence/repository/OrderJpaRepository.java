package tsgp.yah_enterprise.sales.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tsgp.yah_enterprise.sales.infrastructure.persistence.entity.OrderJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID>, JpaSpecificationExecutor<OrderJpaEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrderJpaEntity o WHERE o.orderId = :id")
    Optional<OrderJpaEntity> findForUpdate(@Param("id") UUID id);

    Optional<OrderJpaEntity> findByOrderNumber(String orderNumber);
}
