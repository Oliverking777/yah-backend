package tsgp.yah_enterprise.dashboard.infrastructure.integration;

import tsgp.yah_enterprise.dashboard.domain.port.DashboardLocationScopePort;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.location.application.dto.LocationResult;
import tsgp.yah_enterprise.location.application.port.in.GetLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.GetUserLocationUseCase;

import java.util.*;

/** The dashboard reads Location only through its use cases. A name that cannot be read stays missing. */
@Component
public class DashboardLocationScopeAdapter implements DashboardLocationScopePort {

    private final GetUserLocationUseCase userLocation;
    private final GetLocationUseCase location;

    public DashboardLocationScopeAdapter(GetUserLocationUseCase userLocation, GetLocationUseCase location) {
        this.userLocation = userLocation;
        this.location = location;
    }

    @Override
    public Set<UUID> assignedLocationIds(UUID userId) {
        return userLocation.findLocationOfUser(userId).map(LocationResult::locationId).map(Set::of)
                .orElse(Set.of());
    }

    @Override
    public Map<UUID, String> locationNames(Collection<UUID> ids) {
        Map<UUID, String> out = new HashMap<>();
        for (UUID id : new HashSet<>(ids)) {
            try {
                out.put(id, location.getLocationById(id).name());
            } catch (RuntimeException ignored) {
                // unresolved name stays missing
            }
        }
        return out;
    }
}
