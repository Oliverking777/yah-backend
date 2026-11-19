package tsgp.yah_enterprise.inventory.infrastructure.integration;

import tsgp.yah_enterprise.inventory.domain.port.InventoryLocation;
import tsgp.yah_enterprise.inventory.domain.port.InventoryLocationDirectoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.location.application.dto.LocationResult;
import tsgp.yah_enterprise.location.application.port.in.GetLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.GetUserLocationUseCase;
import tsgp.yah_enterprise.location.domain.exception.LocationNotFoundException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Inventory reads the Location module through its public use cases only; Location's exceptions never escape. */
@Component
@RequiredArgsConstructor
public class InventoryLocationDirectoryAdapter implements InventoryLocationDirectoryPort {

    private static final String ACTIVE = "ACTIVE";

    private final GetLocationUseCase getLocationUseCase;
    private final GetUserLocationUseCase getUserLocationUseCase;

    @Override
    public Optional<InventoryLocation> findLocation(UUID locationId) {
        try {
            return Optional.of(toInventoryLocation(getLocationUseCase.getLocationById(locationId)));
        } catch (LocationNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Map<UUID, InventoryLocation> findAllByIds(Collection<UUID> locationIds) {
        Map<UUID, InventoryLocation> result = new LinkedHashMap<>();
        for (UUID id : new LinkedHashSet<>(locationIds)) {
            findLocation(id).ifPresent(location -> result.put(id, location));
        }
        return result;
    }

    @Override
    public Optional<UUID> findLocationOfUser(UUID userId) {
        return getUserLocationUseCase.findLocationOfUser(userId).map(LocationResult::locationId);
    }

    private InventoryLocation toInventoryLocation(LocationResult location) {
        return new InventoryLocation(location.locationId(), location.name(),
                ACTIVE.equals(location.locationStatus()));
    }
}
