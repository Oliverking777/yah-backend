package tsgp.yah_enterprise.dashboard.domain.port;

import tsgp.yah_enterprise.dashboard.domain.valueobject.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public interface DashboardTransferPort {

    long pendingCount(LocationScope scope);

    PendingTransferPage pendingTransfers(LocationScope scope, int page, int size);

    List<DiscrepancyTransfer> discrepancyTransfers(LocationScope scope, Instant since, int limit);

    /** A transfer in transit (SUBMITTED): sent at sentAt and not yet received or cancelled. */
    record PendingTransfer(UUID transferId, String transferNumber, UUID sourceLocationId, UUID destinationLocationId, Instant sentAt) { }

    record PendingTransferPage(List<PendingTransfer> items, long total) { }

    /** A received transfer with damaged or missing goods. */
    record DiscrepancyTransfer(UUID transferId, String transferNumber, UUID sourceLocationId, UUID destinationLocationId, Instant receivedAt) { }
}