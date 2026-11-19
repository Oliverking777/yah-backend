package tsgp.yah_enterprise.purchasing.domain.port;

import java.util.Optional;
import java.util.UUID;

/** Purchasing's view of the Location module. Implemented in infrastructure/integration. */
public interface PurchaseLocationDirectoryPort {

    Optional<PurchaseLocation> findLocation(UUID locationId);

    Optional<UUID> findLocationIdOfUser(UUID userId);

    default boolean isUserAssigned(UUID userId, UUID locationId) {
        return findLocationIdOfUser(userId).map(locationId::equals).orElse(false);
    }

    record PurchaseLocation(UUID locationId, String locationName, boolean active) { }
}
