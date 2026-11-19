package tsgp.yah_enterprise.reports.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DebtReportRow(UUID customerId, String customerName, UUID locationId, String locationName, long amountDue, long amountPaid, long outstandingBalance) { }
