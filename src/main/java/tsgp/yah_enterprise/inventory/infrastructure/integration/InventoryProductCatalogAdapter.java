package tsgp.yah_enterprise.inventory.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProduct;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProductCatalogPort;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProductDetail;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProductUnit;
import tsgp.yah_enterprise.product.application.dto.VariantSummary;
import tsgp.yah_enterprise.product.application.dto.VariantUnitResult;
import tsgp.yah_enterprise.product.application.port.in.GetVariantsUseCase;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Inventory reads the Product module through its public use case only; Product's exceptions never escape.
 * What is stocked is a variant: the id Inventory keeps is a variant id, shown with the variant's SKU and full name
 * ("Indian Rice · 50 kg 10% broken", or just "Flour" for a simple product). A product's first variant has the
 * product's own id, so stock recorded before variants existed still points at the right variant.
 */
@Component
@RequiredArgsConstructor
public class InventoryProductCatalogAdapter implements InventoryProductCatalogPort {

    private static final String ACTIVE = "ACTIVE";

    private final GetVariantsUseCase getVariantsUseCase;

    @Override
    public Optional<InventoryProduct> findProduct(UUID variantId) {
        return getVariantsUseCase.getVariants(List.of(variantId)).stream().findFirst().map(this::toInventoryProduct);
    }

    /** One call to Product for the whole batch, in the order of the ids given. */
    @Override
    public Map<UUID, InventoryProduct> findAllByIds(Collection<UUID> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, VariantSummary> found = new LinkedHashMap<>();
        for (VariantSummary v : getVariantsUseCase.getVariants(new LinkedHashSet<>(variantIds))) {
            found.put(v.variantId(), v);
        }
        Map<UUID, InventoryProduct> result = new LinkedHashMap<>();
        for (UUID id : new LinkedHashSet<>(variantIds)) {
            VariantSummary v = found.get(id);
            if (v != null) result.put(id, toInventoryProduct(v));
        }
        return result;
    }

    @Override
    public Optional<InventoryProductDetail> findProductDetail(UUID variantId) {
        return getVariantsUseCase.getVariants(List.of(variantId)).stream().findFirst().map(this::toDetail);
    }

    /** active: the product and the variant are both active. */
    private InventoryProduct toInventoryProduct(VariantSummary v) {
        return new InventoryProduct(v.variantId(), v.sku(), v.fullName(), v.usable());
    }

    private InventoryProductDetail toDetail(VariantSummary v) {
        List<InventoryProductUnit> units = v.units().stream().map(this::toInventoryProductUnit).toList();
        return new InventoryProductDetail(v.variantId(), v.sku(), v.fullName(), v.usable(), units);
    }

    private InventoryProductUnit toInventoryProductUnit(VariantUnitResult u) {
        return new InventoryProductUnit(u.unitId(), u.unitCode(), u.unitName(), u.baseUnit(),
                u.conversionFactor(), ACTIVE.equals(u.unitStatus()));
    }
}