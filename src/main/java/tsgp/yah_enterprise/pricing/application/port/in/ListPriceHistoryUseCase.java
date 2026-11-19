package tsgp.yah_enterprise.pricing.application.port.in;

import tsgp.yah_enterprise.pricing.application.dto.PricePageRequest;
import tsgp.yah_enterprise.pricing.application.dto.PricePageResult;

import java.util.UUID;

public interface ListPriceHistoryUseCase {

    /**
     * Returns one page of every price row of a product (active and closed), newest first.
     *
     * @param unitId optional filter; null means every unit
     */
    PricePageResult listPriceHistory(UUID productId, UUID unitId, PricePageRequest request);
}
