package tsgp.yah_enterprise.reports.domain.port;

import tsgp.yah_enterprise.reports.domain.model.*;
import tsgp.yah_enterprise.reports.domain.valueobject.*;

import java.util.*;

/** Reports' view of Purchasing. Implemented in infrastructure/integration. */
public interface ReportPurchasingPort {

    List<PurchasingTotal> purchasingTotals(ReportPeriod period, LocationScope scope, UUID supplierId);

    /**
     * Per supplier. Ordered figures: purchases created in the period that were not cancelled. Delivery figures:
     * purchases confirmed in the period. discrepancyCount is the number of those with rejected or missing goods.
     * Quantities in base units, amounts in whole FCFA.
     */
    record PurchasingTotal(UUID supplierId, long purchaseCount, long orderedQuantity, long orderedAmount,
                           long acceptedQuantity, long rejectedQuantity, long purchaseCost,
                           long discrepancyCount) { }
}