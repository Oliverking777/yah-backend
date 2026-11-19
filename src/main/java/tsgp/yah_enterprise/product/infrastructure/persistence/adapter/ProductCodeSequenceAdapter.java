package tsgp.yah_enterprise.product.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.product.domain.port.ProductCodeGenerator;

/** Product codes from the product_code_seq sequence (V35): PRD-000001, PRD-000002... */
@Component
@RequiredArgsConstructor
public class ProductCodeSequenceAdapter implements ProductCodeGenerator {

    static final String PREFIX = "PRD-";

    private final EntityManager entityManager;

    @Override
    public String nextCode() {
        Number next = (Number) entityManager
                .createNativeQuery("SELECT nextval('public.product_code_seq')")
                .getSingleResult();
        return format(next.longValue());
    }

    /** At least six digits; more once a million products exist. */
    static String format(long number) {
        return PREFIX + String.format("%06d", number);
    }
}