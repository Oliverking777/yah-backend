package tsgp.yah_enterprise.dashboard.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** stage is AWAITING_DISPATCH (SUBMITTED) or AWAITING_RECEIPT (DISPATCHED); since is the submit or dispatch time. */
public record TransferPendingPage(List<Item> items, int page, int size, long totalElements, int totalPages) {

    public record Item(UUID transferId, String transferNumber, String stage, UUID sourceLocationId, String sourceLocationName, UUID destinationLocationId, String destinationLocationName, Instant since) { }
}
