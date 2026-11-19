package tsgp.yah_enterprise.inventory.domain.port;

import java.util.UUID;

/** A location as Inventory sees it. */
public record InventoryLocation(UUID locationId, String locationName, boolean active) {
}
