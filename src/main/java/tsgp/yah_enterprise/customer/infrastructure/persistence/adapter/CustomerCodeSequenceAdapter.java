package tsgp.yah_enterprise.customer.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.customer.application.service.CustomerCodeSequence;

@Component
@RequiredArgsConstructor
public class CustomerCodeSequenceAdapter implements CustomerCodeSequence {

    private final EntityManager em;

    @Override
    public long next() {
        return ((Number) em.createNativeQuery("SELECT nextval('customer_code_seq')")
                .getSingleResult()).longValue();
    }
}
