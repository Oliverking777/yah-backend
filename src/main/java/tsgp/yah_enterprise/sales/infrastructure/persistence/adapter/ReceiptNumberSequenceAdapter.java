package tsgp.yah_enterprise.sales.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.sales.application.service.ReceiptNumberSequence;

@Component
@RequiredArgsConstructor
public class ReceiptNumberSequenceAdapter implements ReceiptNumberSequence {

    private final EntityManager em;

    @Override
    public long next() {
        return ((Number) em.createNativeQuery("SELECT nextval('receipt_number_seq')")
                .getSingleResult()).longValue();
    }
}
