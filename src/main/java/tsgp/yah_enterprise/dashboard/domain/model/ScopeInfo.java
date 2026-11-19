package tsgp.yah_enterprise.dashboard.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** type is ALL or LOCATIONS. */
public record ScopeInfo(String type, List<LocationRef> locations) { }
