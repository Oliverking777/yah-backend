package tsgp.yah_enterprise.inventory.domain.port;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: how Inventory reads the Location module.
 * The adapter lives in infrastructure.integration and calls Location's use cases only.
 */
public interface InventoryLocationDirectoryPort {

    /** Empty when the location does not exist. */
    Optional<InventoryLocation> findLocation(UUID locationId);

    /** Batch lookup for listings. Locations that no longer resolve are simply missing from the map. */
    Map<UUID, InventoryLocation> findAllByIds(Collection<UUID> locationIds);

    /** The location the user is assigned to, or empty when the user has none. */
    Optional<UUID> findLocationOfUser(UUID userId);
}
