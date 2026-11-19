package tsgp.yah_enterprise.transfer.domain.port;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Outbound: reads the Location module. */
public interface TransferLocationDirectoryPort {

    Optional<TransferLocation> findLocation(UUID locationId);

    /** One call for many locations (listings). Ids that no longer resolve are left out. */
    Map<UUID, TransferLocation> findLocations(Collection<UUID> locationIds);

    /** The location a user works at, or empty when the user has none. */
    Optional<UUID> findLocationIdOfUser(UUID userId);

    /** True when the user is assigned to that location. */
    default boolean isUserAssigned(UUID userId, UUID locationId) {
        return locationId != null && findLocationIdOfUser(userId).map(locationId::equals).orElse(false);
    }
}
