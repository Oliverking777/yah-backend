package tsgp.yah_enterprise.supplier.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.supplier.application.service.SupplierCodeSequence;

@Component
@RequiredArgsConstructor
public class SupplierCodeSequenceAdapter implements SupplierCodeSequence {

    private final EntityManager em;

    @Override
    public long next() {
        return ((Number) em.createNativeQuery("SELECT nextval('supplier_code_seq')")
                .getSingleResult()).longValue();
    }
}
