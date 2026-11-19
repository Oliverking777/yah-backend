package tsgp.yah_enterprise.product.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.product.domain.port.ProductUnitInfo;
import tsgp.yah_enterprise.product.domain.port.UnitDirectoryPort;
import tsgp.yah_enterprise.unit.application.port.in.GetUnitUseCase;
import tsgp.yah_enterprise.unit.domain.exception.UnitNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UnitDirectoryAdapter implements UnitDirectoryPort {

    private final GetUnitUseCase getUnitUseCase;

    @Override
    public Optional<ProductUnitInfo> findById(UUID unitId) {
        try {
            var result = getUnitUseCase.getUnitById(unitId);
            return Optional.of(new ProductUnitInfo(
                    result.unitId(), result.unitCode(), result.unitName(), "ACTIVE".equals(result.unitStatus())
            ));
        } catch (UnitNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Resolves each id through {@link #findById(UUID)}. Unknown units are skipped,
     * duplicate and null ids are ignored. GetUnitUseCase has no batch method yet,
     * so this issues one lookup per distinct id.
     */
    @Override
    public List<ProductUnitInfo> findAllByIds(Collection<UUID> unitIds) {
        if (unitIds == null || unitIds.isEmpty()) {
            return List.of();
        }

        return unitIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(this::findById)
                .flatMap(Optional::stream)
                .toList();
    }
}