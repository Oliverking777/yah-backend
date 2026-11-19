package tsgp.yah_enterprise.reports.application.port.in;

import tsgp.yah_enterprise.reports.application.dto.ReportActor;
import tsgp.yah_enterprise.reports.domain.model.StockReport;

import java.util.UUID;

public interface GetStockReportUseCase {
    StockReport getStockReport(ReportActor actor, UUID locationId, UUID productId, String status, int page, int size);
}
