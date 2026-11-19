package tsgp.yah_enterprise.location.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.location.domain.port.LocationCodeGenerator;

/** Location codes from the location_code_seq sequence (V36): LOC-0001, LOC-0002... */
@Component
@RequiredArgsConstructor
public class LocationCodeSequenceAdapter implements LocationCodeGenerator {

    static final String PREFIX = "LOC-";

    private final EntityManager entityManager;

    @Override
    public String nextCode() {
        Number next = (Number) entityManager
                .createNativeQuery("SELECT nextval('public.location_code_seq')")
                .getSingleResult();
        return format(next.longValue());
    }

    /** At least four digits; more once ten thousand locations exist. */
    static String format(long number) {
        return PREFIX + String.format("%04d", number);
    }
}