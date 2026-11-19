package tsgp.yah_enterprise.reports.application.dto;

import tsgp.yah_enterprise.reports.domain.model.ReportName;
import tsgp.yah_enterprise.reports.domain.model.SalesGroupBy;
import tsgp.yah_enterprise.reports.domain.model.TopMetric;

import java.time.LocalDate;
import java.util.UUID;

/** Every filter any report accepts; a report ignores the ones that do not apply to it. */
public record ReportExportRequest(ReportName report, LocalDate from, LocalDate to, UUID locationId,
                                  SalesGroupBy groupBy, UUID productId, String status, UUID supplierId,
                                  UUID customerId, UUID sourceLocationId, UUID destinationLocationId,
                                  Boolean onlyOutstanding, TopMetric metric, Integer limit) { }
