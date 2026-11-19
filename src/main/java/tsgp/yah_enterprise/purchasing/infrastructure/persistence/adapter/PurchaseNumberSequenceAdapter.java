package tsgp.yah_enterprise.purchasing.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.purchasing.application.service.PurchaseNumberSequence;

@Component
@RequiredArgsConstructor
public class PurchaseNumberSequenceAdapter implements PurchaseNumberSequence {

    private final EntityManager em;

    @Override
    public long next() {
        return ((Number) em.createNativeQuery("SELECT nextval('purchase_number_seq')").getSingleResult()).longValue();
    }
}
