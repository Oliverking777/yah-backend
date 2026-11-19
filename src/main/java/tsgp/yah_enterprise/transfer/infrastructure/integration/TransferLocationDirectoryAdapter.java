package tsgp.yah_enterprise.transfer.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.location.application.dto.LocationResult;
import tsgp.yah_enterprise.location.application.port.in.GetLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.GetUserLocationUseCase;
import tsgp.yah_enterprise.location.domain.exception.LocationNotFoundException;
import tsgp.yah_enterprise.transfer.domain.port.TransferLocation;
import tsgp.yah_enterprise.transfer.domain.port.TransferLocationDirectoryPort;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Transfer reads the Location module through its public use cases only. */
@Component
@RequiredArgsConstructor
public class TransferLocationDirectoryAdapter implements TransferLocationDirectoryPort {

    private static final String ACTIVE = "ACTIVE";

    private final GetLocationUseCase getLocationUseCase;
    private final GetUserLocationUseCase getUserLocationUseCase;

    @Override
    public Optional<TransferLocation> findLocation(UUID locationId) {
        try {
            LocationResult l = getLocationUseCase.getLocationById(locationId);
            return Optional.of(new TransferLocation(l.locationId(), l.name(), ACTIVE.equals(l.locationStatus())));
        } catch (LocationNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Map<UUID, TransferLocation> findLocations(Collection<UUID> locationIds) {
        Map<UUID, TransferLocation> found = new HashMap<>();
        for (UUID id : locationIds) {
            findLocation(id).ifPresent(l -> found.put(id, l));
        }
        return found;
    }

    @Override
    public Optional<UUID> findLocationIdOfUser(UUID userId) {
        return getUserLocationUseCase.findLocationOfUser(userId).map(LocationResult::locationId);
    }
}
