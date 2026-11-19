package tsgp.yah_enterprise.dashboard.domain.port;

import tsgp.yah_enterprise.dashboard.domain.valueobject.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public interface DashboardLocationScopePort {

    Set<UUID> assignedLocationIds(UUID userId);

    /** id to name. A name that does not resolve is missing from the map. */
    Map<UUID, String> locationNames(Collection<UUID> locationIds);
}
