package tsgp.yah_enterprise.reports.application.port.in;

import tsgp.yah_enterprise.reports.application.dto.ReportActor;
import tsgp.yah_enterprise.reports.domain.model.SalesGroupBy;
import tsgp.yah_enterprise.reports.domain.model.SalesReport;

import java.time.LocalDate;
import java.util.UUID;

public interface GetSalesReportUseCase {
    SalesReport getSalesReport(ReportActor actor, LocalDate from, LocalDate to, UUID locationId, SalesGroupBy groupBy);
}
