package tsgp.yah_enterprise.transfer.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.transfer.application.service.TransferNumberSequence;

@Component
@RequiredArgsConstructor
public class TransferNumberSequenceAdapter implements TransferNumberSequence {

    private final EntityManager em;

    @Override
    public long next() {
        return ((Number) em.createNativeQuery("SELECT nextval('transfer_number_seq')")
                .getSingleResult()).longValue();
    }
}
