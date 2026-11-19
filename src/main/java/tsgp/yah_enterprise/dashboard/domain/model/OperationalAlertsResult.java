package tsgp.yah_enterprise.dashboard.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OperationalAlertsResult(Instant asOf, ScopeInfo locationScope, List<OperationalAlert> alerts, int totalCount, List<String> unavailableSources) {

    public static final int MAX_ALERTS = 100;
}
