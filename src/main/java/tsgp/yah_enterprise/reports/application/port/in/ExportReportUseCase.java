package tsgp.yah_enterprise.reports.application.port.in;

import tsgp.yah_enterprise.reports.application.dto.ExportedReport;
import tsgp.yah_enterprise.reports.application.dto.ReportActor;
import tsgp.yah_enterprise.reports.application.dto.ReportExportRequest;

public interface ExportReportUseCase {
    /** CSV only, at most 10,000 rows. Every export is written to the audit log. */
    ExportedReport exportReport(ReportActor actor, ReportExportRequest request);
}
