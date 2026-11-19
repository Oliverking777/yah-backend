package tsgp.yah_enterprise.inventory.domain.port;

import java.util.UUID;

/** A product as Inventory sees it: just enough to name it and know whether it is usable. */
public record InventoryProduct(UUID productId, String productCode, String productName, boolean active) {
}
