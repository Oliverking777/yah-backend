package tsgp.yah_enterprise.reports.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SalesReport(LocalDate periodStart, LocalDate periodEnd, Instant asOf, ReportScope scope, SalesGroupBy groupBy, List<SalesReportRow> rows, SalesReportRow totals) { }
