package tsgp.yah_enterprise.dashboard.domain.service;

import tsgp.yah_enterprise.dashboard.domain.exception.DashboardLocationAccessException;
import tsgp.yah_enterprise.dashboard.domain.valueobject.LocationScope;

import java.util.Set;
import java.util.UUID;

/** Decides which locations a dashboard call may cover. The server derives it: the client only asks. */
public class LocationScopePolicy {

    public LocationScope resolve(boolean canViewAll, Set<UUID> assignedLocationIds, UUID requestedLocationId) {
        if (canViewAll) {
            return requestedLocationId == null ? LocationScope.everyLocation()
                    : LocationScope.of(Set.of(requestedLocationId));
        }
        Set<UUID> assigned = assignedLocationIds == null ? Set.of() : assignedLocationIds;
        if (requestedLocationId == null) {
            return LocationScope.of(assigned);
        }
        if (!assigned.contains(requestedLocationId)) {
            throw new DashboardLocationAccessException("You are not assigned to this location");
        }
        return LocationScope.of(Set.of(requestedLocationId));
    }
}
