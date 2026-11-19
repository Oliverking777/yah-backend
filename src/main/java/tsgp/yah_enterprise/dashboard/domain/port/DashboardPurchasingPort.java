package tsgp.yah_enterprise.dashboard.domain.port;

import tsgp.yah_enterprise.dashboard.domain.valueobject.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/** The dashboard's view of Purchasing. Implemented in infrastructure/integration. */
public interface DashboardPurchasingPort {

    /** SUBMITTED purchases: ordered and waiting for the delivery. */
    long awaitingDeliveryCount(LocationScope scope);

    /** Earliest expected delivery first, then oldest. */
    List<AwaitingDelivery> awaitingDelivery(LocationScope scope, int limit);

    /** Purchases confirmed at or after since with goods rejected or missing. Newest first. */
    List<DiscrepancyPurchase> discrepancyPurchases(LocationScope scope, Instant since, int limit);

    /** expectedDeliveryDate may be null. since is when the purchase was sent to the supplier. */
    record AwaitingDelivery(UUID purchaseId, String purchaseNumber, UUID locationId, LocalDate expectedDeliveryDate,
                            Instant since) { }

    record DiscrepancyPurchase(UUID purchaseId, String purchaseNumber, UUID locationId, Instant confirmedAt) { }
}
