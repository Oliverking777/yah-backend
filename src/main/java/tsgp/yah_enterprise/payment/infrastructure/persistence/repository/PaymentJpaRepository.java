package tsgp.yah_enterprise.payment.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tsgp.yah_enterprise.payment.domain.model.PaymentMethod;
import tsgp.yah_enterprise.payment.infrastructure.persistence.entity.PaymentJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository
        extends JpaRepository<PaymentJpaEntity, UUID>, JpaSpecificationExecutor<PaymentJpaEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentJpaEntity p WHERE p.paymentId = :id")
    Optional<PaymentJpaEntity> findForUpdate(@Param("id") UUID id);

    boolean existsByPaymentMethodAndReference(PaymentMethod method, String reference);
}
