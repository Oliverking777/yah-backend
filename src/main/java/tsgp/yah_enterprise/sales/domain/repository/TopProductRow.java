package tsgp.yah_enterprise.sales.domain.repository;

import java.util.UUID;

/** Quantity in base units, value in whole FCFA (line totals after discount). */
public record TopProductRow(UUID productId, long quantitySold, long salesValue) { }
