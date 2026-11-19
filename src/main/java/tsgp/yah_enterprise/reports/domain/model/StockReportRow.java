package tsgp.yah_enterprise.reports.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** valuationAmount is always null in v1: no valuation method is chosen yet. */
public record StockReportRow(UUID locationId, String locationName, UUID productId, String productName, long quantityOnHand, long quantityInTransit, Long lowStockThreshold, StockStatus stockStatus, Long valuationAmount) { }
