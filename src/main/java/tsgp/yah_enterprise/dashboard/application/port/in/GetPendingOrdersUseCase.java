package tsgp.yah_enterprise.dashboard.application.port.in;

import tsgp.yah_enterprise.dashboard.application.dto.DashboardActor;
import tsgp.yah_enterprise.dashboard.domain.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetPendingOrdersUseCase {
    OrderPendingPage getPendingOrders(DashboardActor actor, UUID locationId, int page, int size);
}
