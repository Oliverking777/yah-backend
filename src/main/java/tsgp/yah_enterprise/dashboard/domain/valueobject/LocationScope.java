package tsgp.yah_enterprise.dashboard.domain.valueobject;

import java.util.Set;
import java.util.UUID;

/** Either every location, or a set of location ids (possibly empty: the dashboard is then empty). */
public record LocationScope(boolean all, Set<UUID> locationIds) {

    public LocationScope {
        locationIds = all ? Set.of() : Set.copyOf(locationIds);
    }

    public static LocationScope everyLocation() {
        return new LocationScope(true, Set.of());
    }

    public static LocationScope of(Set<UUID> ids) {
        return new LocationScope(false, ids);
    }

    public boolean contains(UUID locationId) {
        return all || locationIds.contains(locationId);
    }

    public boolean isEmpty() {
        return !all && locationIds.isEmpty();
    }

    /** What the source modules take: null means every location. */
    public Set<UUID> idsOrNull() {
        return all ? null : locationIds;
    }
}
