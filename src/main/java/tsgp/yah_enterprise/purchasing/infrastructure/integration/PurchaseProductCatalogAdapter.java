package tsgp.yah_enterprise.purchasing.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.product.application.dto.VariantSummary;
import tsgp.yah_enterprise.product.application.port.in.GetVariantsUseCase;
import tsgp.yah_enterprise.purchasing.domain.port.PurchaseProductCatalogPort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Purchasing reads the Product module through its public use case only.
 * What is bought is a variant: the id on a purchase line is a variant id, named with its full name
 * ("Indian Rice · 50 kg 10% broken", or just "Flour" for a simple product). A product's first variant has the
 * product's own id, so lines recorded before variants existed still point at the right variant.
 */
@Component
@RequiredArgsConstructor
public class PurchaseProductCatalogAdapter implements PurchaseProductCatalogPort {

    private static final String ACTIVE = "ACTIVE";

    private final GetVariantsUseCase getVariantsUseCase;

    /** active: the product and the variant are both active. */
    @Override
    public Optional<PurchaseProduct> findProduct(UUID variantId) {
        return getVariantsUseCase.getVariants(List.of(variantId)).stream().findFirst().map(this::toPurchaseProduct);
    }

    private PurchaseProduct toPurchaseProduct(VariantSummary v) {
        return new PurchaseProduct(v.variantId(), v.fullName(), v.usable(),
                v.units().stream().map(u -> new PurchaseProductUnit(u.unitId(), ACTIVE.equals(u.unitStatus()),
                        u.conversionFactor())).toList());
    }
}