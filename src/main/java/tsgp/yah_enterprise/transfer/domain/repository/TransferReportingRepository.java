package tsgp.yah_enterprise.transfer.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Read-only figures for reports. locationIds: null means every location, empty means none. */
public interface TransferReportingRepository {

    /** Transfers touching the scope (source or destination), sent (created) in [from, to), not cancelled, per route. */
    List<TransferTotalsRow> totals(Instant from, Instant to, Set<UUID> locationIds, UUID sourceLocationId,
                                   UUID destinationLocationId);

    /** SUBMITTED transfers going to the scope, per destination and product. */
    List<InTransitRow> inTransit(Set<UUID> destinationLocationIds);
}