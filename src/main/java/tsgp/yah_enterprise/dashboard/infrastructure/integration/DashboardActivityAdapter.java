package tsgp.yah_enterprise.dashboard.infrastructure.integration;

import tsgp.yah_enterprise.dashboard.domain.port.DashboardActivityPort;
import tsgp.yah_enterprise.dashboard.domain.valueobject.LocationScope;
import org.springframework.stereotype.Component;

import java.util.List;

/** The dashboard reads the audit log only through Audit's GetRecentActivityUseCase. */
@Component
public class DashboardActivityAdapter implements DashboardActivityPort {

    private final tsgp.yah_enterprise.audit.application.port.in.GetRecentActivityUseCase recentActivity;

    public DashboardActivityAdapter(
            tsgp.yah_enterprise.audit.application.port.in.GetRecentActivityUseCase recentActivity) {
        this.recentActivity = recentActivity;
    }

    @Override
    public List<Activity> recent(LocationScope scope, int limit) {
        return recentActivity.getRecentActivity(scope.idsOrNull(), limit).stream()
                .map(e -> new Activity(e.occurredAt(), e.actorId(), e.action(), e.entityType(), e.entityId(),
                        e.locationId(), e.outcome())).toList();
    }
}
