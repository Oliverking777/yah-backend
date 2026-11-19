package tsgp.yah_enterprise.product.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * A variant to create with the units it is counted in. Its SKU is generated from the product code.
 *
 * @param variantName e.g. "50 kg 10% broken"; empty only for the single variant of a simple product
 * @param baseUnitId  the unit counted with factor 1, e.g. Bag
 * @param units       its other units, each with how many base units it holds; may be empty
 */
public record NewVariantCommand(String variantName, UUID baseUnitId, List<Unit> units) {

    public NewVariantCommand {
        units = units == null ? List.of() : List.copyOf(units);
    }

    /** 1 of this unit = conversionFactor base units (2 or more). */
    public record Unit(UUID unitId, int conversionFactor) {}
}
