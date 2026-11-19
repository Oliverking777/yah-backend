package tsgp.yah_enterprise.reports.domain.port;

import tsgp.yah_enterprise.reports.domain.model.*;
import tsgp.yah_enterprise.reports.domain.valueobject.*;

import java.util.*;

/** Reports' view of Location. Implemented in infrastructure/integration. */
public interface ReportLocationScopePort {

    Set<UUID> assignedLocationIds(UUID userId);

    /** id to name. A name that does not resolve is missing from the map. */
    Map<UUID, String> locationNames(Collection<UUID> locationIds);
}
