package tsgp.yah_enterprise.dashboard.application.port.in;

import tsgp.yah_enterprise.dashboard.application.dto.DashboardActor;
import tsgp.yah_enterprise.dashboard.domain.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetSalesByLocationUseCase {
    /** Default period is today. */
    List<LocationSalesSummary> getSalesByLocation(DashboardActor actor, LocalDate from, LocalDate to);
}
