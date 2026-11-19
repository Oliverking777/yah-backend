package tsgp.yah_enterprise.payment.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tsgp.yah_enterprise.payment.infrastructure.persistence.entity.PaymentAllocationJpaEntity;

import java.util.List;
import java.util.UUID;

public interface PaymentAllocationJpaRepository extends JpaRepository<PaymentAllocationJpaEntity, UUID> {

    List<PaymentAllocationJpaEntity> findAllByPaymentIdOrderByCreatedAtAsc(UUID paymentId);

    List<PaymentAllocationJpaEntity> findAllBySaleIdOrderByCreatedAtAsc(UUID saleId);
}
