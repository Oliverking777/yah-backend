package tsgp.yah_enterprise.reports.application.port.in;

import tsgp.yah_enterprise.reports.application.dto.ReportActor;
import tsgp.yah_enterprise.reports.domain.model.PurchasingReport;

import java.time.LocalDate;
import java.util.UUID;

public interface GetPurchasingReportUseCase {
    PurchasingReport getPurchasingReport(ReportActor actor, LocalDate from, LocalDate to, UUID locationId,
                                         UUID supplierId);
}
