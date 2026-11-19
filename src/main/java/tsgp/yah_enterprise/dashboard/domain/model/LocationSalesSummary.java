package tsgp.yah_enterprise.dashboard.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LocationSalesSummary(UUID locationId, String locationName, long completedSalesCount, long completedSalesAmount, long paymentsCollectedAmount) { }
