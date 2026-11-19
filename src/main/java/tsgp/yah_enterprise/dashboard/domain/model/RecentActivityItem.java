package tsgp.yah_enterprise.dashboard.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecentActivityItem(Instant occurredAt, UUID actorId, String action, String entityType, UUID entityId, UUID locationId, String outcome) { }
