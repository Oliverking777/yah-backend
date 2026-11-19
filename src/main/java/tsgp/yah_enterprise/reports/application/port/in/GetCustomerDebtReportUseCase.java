package tsgp.yah_enterprise.reports.application.port.in;

import tsgp.yah_enterprise.reports.application.dto.ReportActor;
import tsgp.yah_enterprise.reports.domain.model.DebtReport;

import java.time.LocalDate;
import java.util.UUID;

public interface GetCustomerDebtReportUseCase {
    DebtReport getCustomerDebtReport(ReportActor actor, LocalDate from, LocalDate to, UUID locationId,
                                     UUID customerId, boolean onlyOutstanding);
}
