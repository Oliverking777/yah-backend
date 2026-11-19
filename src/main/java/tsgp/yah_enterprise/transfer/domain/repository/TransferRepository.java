package tsgp.yah_enterprise.transfer.domain.repository;

import tsgp.yah_enterprise.transfer.domain.model.Transfer;
import tsgp.yah_enterprise.transfer.domain.model.TransferEvent;
import tsgp.yah_enterprise.transfer.domain.model.TransferStatus;
import tsgp.yah_enterprise.transfer.domain.valueobject.TransferNumber;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepository {

    /** Saves the transfer with its lines and appends the transfer's new events. Returns the stored transfer. */
    Transfer save(Transfer transfer);

    Optional<Transfer> findById(UUID transferId);

    /** Reads the transfer and locks it until the end of the transaction, so a receipt and a cancel cannot overlap. */
    Optional<Transfer> findByIdForUpdate(UUID transferId);

    Optional<Transfer> findByNumber(TransferNumber number);

    /**
     * Newest first. Any filter may be null. A locationId matches the source or the destination.
     * hasDiscrepancy: true for receipts with damaged or missing goods, false for the others, null for all.
     * from is inclusive and to is exclusive, both on the creation time.
     */
    PagedTransfers findAll(int page, int size, UUID locationId, TransferStatus status, Boolean hasDiscrepancy,
                           UUID productId, Instant from, Instant to);

    /** Transfers in SUBMITTED status (in transit). A locationId matches the source or the destination. */
    List<Transfer> findInTransit(UUID locationId);

    /** Oldest first. */
    List<TransferEvent> findEvents(UUID transferId);
}