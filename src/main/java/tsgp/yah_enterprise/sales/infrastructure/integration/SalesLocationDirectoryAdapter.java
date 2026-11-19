package tsgp.yah_enterprise.sales.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.location.application.dto.LocationResult;
import tsgp.yah_enterprise.location.application.port.in.GetLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.GetUserLocationUseCase;
import tsgp.yah_enterprise.location.domain.exception.LocationNotFoundException;
import tsgp.yah_enterprise.sales.domain.port.SalesLocationDirectoryPort;

import java.util.Optional;
import java.util.UUID;

/** Sales reads the Location module through its public use cases only. */
@Component
@RequiredArgsConstructor
public class SalesLocationDirectoryAdapter implements SalesLocationDirectoryPort {

    private final GetLocationUseCase getLocationUseCase;
    private final GetUserLocationUseCase getUserLocationUseCase;

    @Override
    public Optional<SalesLocation> findLocation(UUID locationId) {
        try {
            LocationResult l = getLocationUseCase.getLocationById(locationId);
            return Optional.of(new SalesLocation(l.locationId(), l.name(), "ACTIVE".equals(l.locationStatus())));
        } catch (LocationNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UUID> findLocationIdOfUser(UUID userId) {
        return getUserLocationUseCase.findLocationOfUser(userId).map(LocationResult::locationId);
    }
}
