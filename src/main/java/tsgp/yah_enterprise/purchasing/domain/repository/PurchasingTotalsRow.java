package tsgp.yah_enterprise.purchasing.domain.repository;

import java.util.UUID;

/**
 * Per supplier. Ordered figures count purchases created in the period that were not cancelled. Delivery figures count
 * CONFIRMED purchases by confirmedAt: purchaseCost = accepted quantity x the line's purchase price, and
 * discrepancyCount is the number of those with goods rejected or missing.
 * Quantities in base units, amounts in whole FCFA.
 */
public record PurchasingTotalsRow(UUID supplierId, long purchaseCount, long orderedQuantity, long orderedAmount,
                                  long acceptedQuantity, long rejectedQuantity, long purchaseCost,
                                  long discrepancyCount) { }
