package tsgp.yah_enterprise.product.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * A variant to create, with the units it is counted in: its base unit, and each other unit with how many base units
 * it holds (e.g. Bag as base, and nothing else; or Bottle as base and Carton = 24). The SKU is generated.
 *
 * @param name       e.g. "50 kg 10% broken"; null or blank only for the single variant of a simple product
 * @param baseUnitId the unit counted with factor 1
 * @param units      the other units; may be empty
 */
public record NewVariant(String name, UUID baseUnitId, List<NewUnit> units) {

    public NewVariant {
        units = units == null ? List.of() : List.copyOf(units);
    }

    /** 1 of this unit = conversionFactor base units; the factor is 2 or more. */
    public record NewUnit(UUID unitId, int conversionFactor) {}
}