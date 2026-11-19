package tsgp.yah_enterprise.reports.application.port.in;

import tsgp.yah_enterprise.reports.application.dto.ReportActor;
import tsgp.yah_enterprise.reports.domain.model.TopMetric;
import tsgp.yah_enterprise.reports.domain.model.TopProductsReport;

import java.time.LocalDate;
import java.util.UUID;

public interface GetTopSellingProductsReportUseCase {
    /** limit defaults to 10 and must be between 1 and 50. */
    TopProductsReport getTopSellingProducts(ReportActor actor, LocalDate from, LocalDate to, UUID locationId,
                                            TopMetric metric, Integer limit);
}
