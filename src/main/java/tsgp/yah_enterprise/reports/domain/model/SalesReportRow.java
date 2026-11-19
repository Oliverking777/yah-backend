package tsgp.yah_enterprise.reports.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** gross = sum of order subtotals, net = gross - discount. Collected is cash received, a different number from sales. */
public record SalesReportRow(String groupKey, String groupLabel, long completedSalesCount, long grossSalesAmount, long discountAmount, long netSalesAmount, long paymentCollectedAmount, long outstandingAmount) { }
