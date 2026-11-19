package tsgp.yah_enterprise.dashboard.domain.port;

import tsgp.yah_enterprise.dashboard.domain.valueobject.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/** Business actions in scope. Security entries are not shown. */
public interface DashboardActivityPort {

    List<Activity> recent(LocationScope scope, int limit);

    record Activity(Instant occurredAt, UUID actorId, String action, String entityType, UUID entityId, UUID locationId, String outcome) { }
}
