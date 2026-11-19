package tsgp.yah_enterprise.inventory.domain.port;

import java.util.UUID;

/** A unit as Inventory sees it during stock initialization: enough to validate and convert. */
public record InventoryProductUnit(
        UUID unitId,
        String unitCode,
        String unitName,
        boolean baseUnit,
        int conversionFactor,
        boolean active
) {
}
