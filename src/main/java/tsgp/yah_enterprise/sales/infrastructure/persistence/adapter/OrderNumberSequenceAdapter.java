package tsgp.yah_enterprise.sales.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.sales.application.service.OrderNumberSequence;

@Component
@RequiredArgsConstructor
public class OrderNumberSequenceAdapter implements OrderNumberSequence {

    private final EntityManager em;

    @Override
    public long next() {
        return ((Number) em.createNativeQuery("SELECT nextval('order_number_seq')")
                .getSingleResult()).longValue();
    }
}
