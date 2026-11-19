package tsgp.yah_enterprise.payment.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.location.application.dto.LocationResult;
import tsgp.yah_enterprise.location.application.port.in.GetLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.GetUserLocationUseCase;
import tsgp.yah_enterprise.location.domain.exception.LocationNotFoundException;
import tsgp.yah_enterprise.payment.domain.port.PaymentLocationDirectoryPort;

import java.util.Optional;
import java.util.UUID;

/** Payment reads the Location module through its public use cases only. */
@Component
@RequiredArgsConstructor
public class PaymentLocationDirectoryAdapter implements PaymentLocationDirectoryPort {

    private static final String ACTIVE = "ACTIVE";

    private final GetLocationUseCase getLocationUseCase;
    private final GetUserLocationUseCase getUserLocationUseCase;

    @Override
    public Optional<UUID> assignedLocationOf(UUID userId) {
        return getUserLocationUseCase.findLocationOfUser(userId).map(LocationResult::locationId);
    }

    @Override
    public boolean isActive(UUID locationId) {
        try {
            return ACTIVE.equals(getLocationUseCase.getLocationById(locationId).locationStatus());
        } catch (LocationNotFoundException e) {
            return false;
        }
    }
}
