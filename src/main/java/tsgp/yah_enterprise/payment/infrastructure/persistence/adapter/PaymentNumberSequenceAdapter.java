package tsgp.yah_enterprise.payment.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.payment.application.service.PaymentNumberSequence;

@Component
@RequiredArgsConstructor
public class PaymentNumberSequenceAdapter implements PaymentNumberSequence {

    private final EntityManager em;

    @Override
    public long next() {
        return ((Number) em.createNativeQuery("SELECT nextval('payment_number_seq')")
                .getSingleResult()).longValue();
    }
}
