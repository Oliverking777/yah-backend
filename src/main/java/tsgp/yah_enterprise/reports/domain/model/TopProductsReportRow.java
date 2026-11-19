package tsgp.yah_enterprise.reports.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TopProductsReportRow(int rank, UUID productId, String productName, long quantitySold, long salesValue) { }
