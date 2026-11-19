package tsgp.yah_enterprise.inventory.presentation.dto;

import tsgp.yah_enterprise.inventory.application.dto.StockInitializationItemResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "One product line in a stock initialization")
public record StockInitializationItemResponse(
        @Schema(description = "Item id") UUID itemId,
        @Schema(description = "Product id") UUID productId,
        @Schema(description = "Product name") String productName,
        @Schema(description = "Unit id") UUID unitId,
        @Schema(description = "Unit code (e.g. CTN)") String unitCode,
        @Schema(description = "Unit name (e.g. Carton)") String unitName,
        @Schema(description = "Conversion factor to base unit") int conversionFactor,
        @Schema(description = "Counted quantity in the selected unit") long countedQuantity,
        @Schema(description = "Quantity in base units (posted to the ledger)") long baseQuantity
) {
    public static StockInitializationItemResponse from(StockInitializationItemResult r) {
        return new StockInitializationItemResponse(r.itemId(), r.productId(), r.productName(),
                r.unitId(), r.unitCode(), r.unitName(), r.conversionFactor(),
                r.countedQuantity(), r.baseQuantity());
    }
}
