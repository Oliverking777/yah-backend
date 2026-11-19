package tsgp.yah_enterprise.unit.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.unit.domain.port.UnitCodeGenerator;

/**
 * Unit codes from the unit_code_seq sequence (V37): U0001, U0002...
 * Letters and digits only, because a unit code allows nothing else (1 to 10 characters).
 */
@Component
@RequiredArgsConstructor
public class UnitCodeSequenceAdapter implements UnitCodeGenerator {

    static final String PREFIX = "U";

    private final EntityManager entityManager;

    @Override
    public String nextCode() {
        Number next = (Number) entityManager
                .createNativeQuery("SELECT nextval('public.unit_code_seq')")
                .getSingleResult();
        return format(next.longValue());
    }

    /** At least four digits; more once ten thousand units exist (up to U999999999). */
    static String format(long number) {
        return PREFIX + String.format("%04d", number);
    }
}