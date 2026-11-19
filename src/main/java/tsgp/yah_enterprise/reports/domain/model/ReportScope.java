package tsgp.yah_enterprise.reports.domain.model;

import java.util.List;
import java.util.UUID;

/** The scope a report was built for: ALL, or the listed locations. */
public record ReportScope(String type, List<UUID> locationIds) {
    public static ReportScope from(boolean all, java.util.Set<UUID> ids) {
        return new ReportScope(all ? "ALL" : "LOCATIONS", all ? List.of() : ids.stream().sorted().toList());
    }
}
