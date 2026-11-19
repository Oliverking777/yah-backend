package tsgp.yah_enterprise.reports.domain.model;

import java.util.UUID;

/**
 * One supplier over the period. Ordered figures: purchases created in the period and not cancelled. Delivery
 * figures: purchases confirmed in the period (accepted went into stock, rejected was refused, purchaseCost is what
 * the accepted goods cost, discrepancyCount the deliveries with rejected or missing goods).
 * Quantities in base units, amounts in whole FCFA. On the totals row supplierId is null and supplierName "Total".
 */
public record PurchasingReportRow(UUID supplierId, String supplierName, long purchaseCount, long orderedQuantity, long orderedAmount, long acceptedQuantity, long rejectedQuantity, long purchaseCost, long discrepancyCount) { }