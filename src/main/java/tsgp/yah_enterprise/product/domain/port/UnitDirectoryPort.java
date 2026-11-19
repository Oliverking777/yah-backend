package tsgp.yah_enterprise.product.domain.port;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnitDirectoryPort {
    Optional<ProductUnitInfo> findById(UUID unitId);
    List<ProductUnitInfo> findAllByIds(Collection<UUID> unitIds);
}
