package tsgp.yah_enterprise.product.application.port.in;

import tsgp.yah_enterprise.product.application.dto.VariantSummary;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Read side for other modules (stock, prices, documents) that keep variant ids. */
public interface GetVariantsUseCase {

    /** Unknown ids are skipped; each variant once. */
    List<VariantSummary> getVariants(Collection<UUID> variantIds);

    /** The variant with this SKU (any case), e.g. typed at a counter. */
    Optional<VariantSummary> findVariantBySku(String sku);
}
