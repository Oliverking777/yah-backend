package tsgp.yah_enterprise.dashboard.application.port.in;

import tsgp.yah_enterprise.dashboard.application.dto.DashboardActor;
import tsgp.yah_enterprise.dashboard.domain.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetRecentActivityUseCase {
    /** limit defaults to 20, up to 50. */
    List<RecentActivityItem> getRecentActivity(DashboardActor actor, Integer limit);
}
