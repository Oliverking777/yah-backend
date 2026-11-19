package tsgp.yah_enterprise.reports.domain.port;

import tsgp.yah_enterprise.reports.domain.model.*;
import tsgp.yah_enterprise.reports.domain.valueobject.*;

import java.util.*;

/** Reports' view of Transfer. Implemented in infrastructure/integration. */
public interface ReportTransferPort {

    List<TransferTotal> transferTotals(ReportPeriod period, LocationScope scope, UUID sourceLocationId,
                                       UUID destinationLocationId);

    List<InTransit> inTransitByDestination(LocationScope scope);

    /** Per route, base units. sentQuantity: goods sent in the period (cancelled transfers excluded). */
    record TransferTotal(UUID sourceLocationId, UUID destinationLocationId, long sentQuantity,
                         long receivedQuantity, long damagedQuantity, long missingQuantity,
                         long pendingTransferCount, long discrepancyCount) { }

    record InTransit(UUID destinationLocationId, UUID productId, long quantity) { }
}