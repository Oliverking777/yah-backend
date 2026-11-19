package tsgp.yah_enterprise.transfer.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** What Transfer knows about a product. */
public record TransferProduct(UUID productId, String productCode, String productName, boolean active,
                              List<TransferProductUnit> units) {

    public TransferProduct {
        units = units == null ? List.of() : List.copyOf(units);
    }

    public Optional<TransferProductUnit> unit(UUID unitId) {
        return units.stream().filter(u -> u.unitId().equals(unitId)).findFirst();
    }
}
