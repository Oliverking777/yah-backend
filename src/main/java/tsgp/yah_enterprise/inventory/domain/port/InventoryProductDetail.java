package tsgp.yah_enterprise.inventory.domain.port;

import java.util.List;
import java.util.UUID;

/** A product as Inventory sees it during stock initialization: the product plus its available units. */
public record InventoryProductDetail(
        UUID productId,
        String productCode,
        String productName,
        boolean active,
        List<InventoryProductUnit> units
) {
    public InventoryProductDetail {
        units = units == null ? List.of() : List.copyOf(units);
    }
}
