package tsgp.yah_enterprise.dashboard.application.dto;

import java.util.UUID;

/** Who asks, and whether they may see every location (DASHBOARD_VIEW_ALL). */
public record DashboardActor(UUID userId, boolean canViewAll) { }
