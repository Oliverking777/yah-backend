package tsgp.yah_enterprise.dashboard.application.port.in;

import tsgp.yah_enterprise.dashboard.application.dto.DashboardActor;
import tsgp.yah_enterprise.dashboard.domain.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetLowStockAlertsUseCase {
    /** OUT_OF_STOCK and LOW_STOCK only. limit defaults to 20, between 1 and 100. */
    List<OperationalAlert> getLowStockAlerts(DashboardActor actor, UUID locationId, Integer limit);
}
