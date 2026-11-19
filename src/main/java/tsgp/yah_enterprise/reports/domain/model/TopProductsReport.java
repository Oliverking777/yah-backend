package tsgp.yah_enterprise.reports.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TopProductsReport(LocalDate periodStart, LocalDate periodEnd, Instant asOf, ReportScope scope, TopMetric metric, int limit, List<TopProductsReportRow> rows) { }
