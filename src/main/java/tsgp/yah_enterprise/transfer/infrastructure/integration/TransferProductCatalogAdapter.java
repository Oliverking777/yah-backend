package tsgp.yah_enterprise.transfer.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.product.application.dto.VariantSummary;
import tsgp.yah_enterprise.product.application.port.in.GetVariantsUseCase;
import tsgp.yah_enterprise.transfer.domain.port.TransferProduct;
import tsgp.yah_enterprise.transfer.domain.port.TransferProductCatalogPort;
import tsgp.yah_enterprise.transfer.domain.port.TransferProductUnit;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Transfer reads the Product module through its public use case only.
 * What moves is a variant: the id on a transfer line is a variant id, shown with its SKU and full name
 * ("Indian Rice · 50 kg 10% broken", or just "Flour" for a simple product). A product's first variant has the
 * product's own id, so lines recorded before variants existed still point at the right variant.
 */
@Component
@RequiredArgsConstructor
public class TransferProductCatalogAdapter implements TransferProductCatalogPort {

    private static final String ACTIVE = "ACTIVE";

    private final GetVariantsUseCase getVariantsUseCase;

    @Override
    public Optional<TransferProduct> findProduct(UUID variantId) {
        return getVariantsUseCase.getVariants(List.of(variantId)).stream().findFirst().map(this::toTransferProduct);
    }

    /** One call to Product for the whole page. */
    @Override
    public Map<UUID, TransferProduct> findProducts(Collection<UUID> variantIds) {
        Map<UUID, TransferProduct> found = new HashMap<>();
        if (variantIds == null || variantIds.isEmpty()) {
            return found;
        }
        for (VariantSummary v : getVariantsUseCase.getVariants(variantIds)) {
            found.put(v.variantId(), toTransferProduct(v));
        }
        return found;
    }

    /** active: the product and the variant are both active. */
    private TransferProduct toTransferProduct(VariantSummary v) {
        List<TransferProductUnit> units = v.units().stream()
                .map(u -> new TransferProductUnit(u.unitId(), u.unitCode(), u.unitName(),
                        ACTIVE.equals(u.unitStatus()), u.conversionFactor()))
                .toList();
        return new TransferProduct(v.variantId(), v.sku(), v.fullName(), v.usable(), units);
    }
}