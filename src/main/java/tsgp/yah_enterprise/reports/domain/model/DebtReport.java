package tsgp.yah_enterprise.reports.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DebtReport(LocalDate periodStart, LocalDate periodEnd, Instant asOf, ReportScope scope, boolean onlyOutstanding, List<DebtReportRow> rows, DebtReportRow totals) { }
