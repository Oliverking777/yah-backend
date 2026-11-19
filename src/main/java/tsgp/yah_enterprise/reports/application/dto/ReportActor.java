package tsgp.yah_enterprise.reports.application.dto;

import java.util.UUID;

/** Who asks for a report, and whether they may see every location (REPORT_VIEW_ALL). */
public record ReportActor(UUID userId, boolean canViewAll) { }
