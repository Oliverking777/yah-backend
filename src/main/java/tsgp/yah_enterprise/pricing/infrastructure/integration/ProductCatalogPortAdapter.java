package tsgp.yah_enterprise.pricing.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.pricing.domain.port.PriceableProduct;
import tsgp.yah_enterprise.pricing.domain.port.PriceableUnit;
import tsgp.yah_enterprise.pricing.domain.port.ProductCatalogPort;
import tsgp.yah_enterprise.product.application.dto.VariantSummary;
import tsgp.yah_enterprise.product.application.dto.VariantUnitResult;
import tsgp.yah_enterprise.product.application.port.in.GetVariantsUseCase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Pricing reads the Product module through its public use case only.
 * A price belongs to a variant: the id Pricing keeps is a variant id. A product's first variant has the product's
 * own id, so prices recorded before variants existed still point at the right variant.
 */
@Component
@RequiredArgsConstructor
public class ProductCatalogPortAdapter implements ProductCatalogPort {

    private static final String ACTIVE = "ACTIVE";

    private final GetVariantsUseCase getVariantsUseCase;

    @Override
    public Optional<PriceableProduct> findProduct(UUID variantId) {
        return getVariantsUseCase.getVariants(List.of(variantId)).stream()
                .findFirst()
                .map(this::toPriceable);
    }

    /** usable: the product and the variant are both active. */
    private PriceableProduct toPriceable(VariantSummary variant) {
        return new PriceableProduct(variant.variantId(), variant.usable(),
                variant.units().stream().map(this::toPriceableUnit).toList());
    }

    private PriceableUnit toPriceableUnit(VariantUnitResult unit) {
        return new PriceableUnit(unit.unitId(), ACTIVE.equals(unit.unitStatus()));
    }
}