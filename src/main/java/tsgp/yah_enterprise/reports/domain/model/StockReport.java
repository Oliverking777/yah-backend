package tsgp.yah_enterprise.reports.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record StockReport(Instant asOf, ReportScope scope, List<StockReportRow> rows, int page, int size, long totalElements, int totalPages) { }
