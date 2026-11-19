package tsgp.yah_enterprise.payment.domain.repository;

import tsgp.yah_enterprise.payment.domain.model.Payment;
import tsgp.yah_enterprise.payment.domain.model.PaymentMethod;
import tsgp.yah_enterprise.payment.domain.model.PaymentStatus;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID paymentId);

    /** Locks the row until the end of the transaction. */
    Optional<Payment> findByIdForUpdate(UUID paymentId);

    boolean existsByMethodAndReference(PaymentMethod method, String reference);

    /** Any filter may be null. locationId null means every location. */
    PageSlice<Payment> search(UUID customerId, UUID locationId, PaymentStatus status, int page, int size);
}
