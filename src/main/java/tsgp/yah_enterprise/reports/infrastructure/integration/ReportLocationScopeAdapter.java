package tsgp.yah_enterprise.reports.infrastructure.integration;

import tsgp.yah_enterprise.reports.domain.port.ReportLocationScopePort;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.location.application.dto.LocationResult;
import tsgp.yah_enterprise.location.application.port.in.GetLocationUseCase;
import tsgp.yah_enterprise.location.application.port.in.GetUserLocationUseCase;

import java.util.*;

/** Reports reads Location only through its use cases. A name that cannot be read stays missing. */
@Component
public class ReportLocationScopeAdapter implements ReportLocationScopePort {

    private final GetUserLocationUseCase userLocation;
    private final GetLocationUseCase location;

    public ReportLocationScopeAdapter(GetUserLocationUseCase userLocation, GetLocationUseCase location) {
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
