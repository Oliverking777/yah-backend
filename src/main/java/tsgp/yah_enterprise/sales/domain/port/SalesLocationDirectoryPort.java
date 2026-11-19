package tsgp.yah_enterprise.sales.domain.port;

import java.util.Optional;
import java.util.UUID;

/** Sales' view of the Location module. Implemented in infrastructure/integration. */
public interface SalesLocationDirectoryPort {

    Optional<SalesLocation> findLocation(UUID locationId);

    Optional<UUID> findLocationIdOfUser(UUID userId);

    default boolean isUserAssigned(UUID userId, UUID locationId) {
        return findLocationIdOfUser(userId).map(locationId::equals).orElse(false);
    }

    record SalesLocation(UUID locationId, String locationName, boolean active) { }
}
