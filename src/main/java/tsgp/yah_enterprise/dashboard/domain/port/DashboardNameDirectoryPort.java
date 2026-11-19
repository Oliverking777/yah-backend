package tsgp.yah_enterprise.dashboard.domain.port;

import tsgp.yah_enterprise.dashboard.domain.valueobject.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/** Product names for alert messages. A name that does not resolve is missing from the map. */
public interface DashboardNameDirectoryPort {

    Map<UUID, String> productNames(Collection<UUID> productIds);
}
