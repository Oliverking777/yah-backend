package tsgp.yah_enterprise.transfer.domain.model;

import tsgp.yah_enterprise.transfer.domain.exception.*;
import tsgp.yah_enterprise.transfer.domain.valueobject.TransferNumber;

import java.time.Instant;
import java.util.*;

/**
 * A movement of products between two locations. Creating it sends the goods: the transfer starts SUBMITTED
 * (in transit) and is only complete when the destination confirms what arrived.
 * <p>
 * Allowed moves: SUBMITTED to RECEIVED, SUBMITTED to CANCELLED. Both are final.
 * The status is the lifecycle only. How the receipt went is a separate outcome: a shortfall or damage keeps the
 * status RECEIVED, sets {@link #hasDiscrepancy()} and is detailed on the lines.
 * The aggregate checks the current status and throws when a move is not allowed. It knows nothing about
 * stock: the application service writes the stock movements in the same transaction
 * (source out on submit, destination in on receipt, source back in on cancel).
 */
public class Transfer {

    public static final int MAX_ITEMS = 100;
    public static final int MAX_NOTE_LENGTH = 500;
    public static final int MIN_REASON_LENGTH = 5;
    public static final int MAX_REASON_LENGTH = 500;

    private final UUID transferId;
    private final TransferNumber transferNumber;
    private final UUID sourceLocationId;
    private final UUID destinationLocationId;
    private TransferStatus transferStatus;
    /** Set at receipt: true when at least one line arrived damaged or short. Always false before receipt. */
    private boolean hasDiscrepancy;
    private final List<TransferItem> items;
    private final String note;
    private final UUID createdBy;
    private UUID receivedBy;
    private UUID cancelledBy;
    private String cancelReason;
    private final Instant createdAt;
    private Instant receivedAt;
    private Instant cancelledAt;
    private Instant updatedAt;

    /** Events created since the transfer was loaded; the repository appends them when it saves. */
    private final List<TransferEvent> pendingEvents = new ArrayList<>();

    private Transfer(UUID transferId, TransferNumber transferNumber, UUID sourceLocationId,
                     UUID destinationLocationId, TransferStatus transferStatus, boolean hasDiscrepancy,
                     List<TransferItem> items, String note, UUID createdBy, UUID receivedBy, UUID cancelledBy,
                     String cancelReason, Instant createdAt, Instant receivedAt, Instant cancelledAt,
                     Instant updatedAt) {
        this.transferId = Objects.requireNonNull(transferId, "transferId");
        this.transferNumber = Objects.requireNonNull(transferNumber, "transferNumber");
        this.sourceLocationId = Objects.requireNonNull(sourceLocationId, "sourceLocationId");
        this.destinationLocationId = Objects.requireNonNull(destinationLocationId, "destinationLocationId");
        this.transferStatus = Objects.requireNonNull(transferStatus, "transferStatus");
        if (hasDiscrepancy && transferStatus != TransferStatus.RECEIVED) {
            throw new IllegalStateException("Only a RECEIVED transfer can have a discrepancy");
        }
        this.hasDiscrepancy = hasDiscrepancy;
        this.items = new ArrayList<>(items);
        this.note = note;
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy");
        this.receivedBy = receivedBy;
        this.cancelledBy = cancelledBy;
        this.cancelReason = cancelReason;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.receivedAt = receivedAt;
        this.cancelledAt = cancelledAt;
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    /**
     * A new transfer, sent at once: it starts SUBMITTED.
     * transferId is the client's UUID for offline work, or null to let the domain generate one.
     */
    public static Transfer submit(UUID transferId, TransferNumber number, UUID sourceLocationId,
                                  UUID destinationLocationId, String note, List<TransferItem> items,
                                  UUID createdBy) {
        if (sourceLocationId == null || destinationLocationId == null) {
            throw new InvalidTransferException("Source and destination location are required");
        }
        if (createdBy == null) {
            throw new InvalidTransferException("The user creating the transfer is required");
        }
        if (sourceLocationId.equals(destinationLocationId)) {
            throw new SameLocationTransferException();
        }
        validateItems(items);
        Instant now = Instant.now();
        Transfer transfer = new Transfer(transferId == null ? UUID.randomUUID() : transferId, number,
                sourceLocationId, destinationLocationId, TransferStatus.SUBMITTED, false, items, cleanNote(note),
                createdBy, null, null, null, now, null, null, now);
        transfer.record(TransferEventType.SUBMITTED, null, TransferStatus.SUBMITTED, createdBy, null);
        return transfer;
    }

    public static Transfer reconstitute(UUID transferId, TransferNumber transferNumber, UUID sourceLocationId,
                                        UUID destinationLocationId, TransferStatus transferStatus,
                                        boolean hasDiscrepancy, List<TransferItem> items, String note,
                                        UUID createdBy, UUID receivedBy, UUID cancelledBy, String cancelReason,
                                        Instant createdAt, Instant receivedAt, Instant cancelledAt,
                                        Instant updatedAt) {
        return new Transfer(transferId, transferNumber, sourceLocationId, destinationLocationId, transferStatus,
                hasDiscrepancy, items, note, createdBy, receivedBy, cancelledBy, cancelReason, createdAt, receivedAt,
                cancelledAt, updatedAt);
    }

    // ------------------------------------------------------------------ transitions

    /**
     * Receipt. Every line needs an entry. Per line: received + damaged cannot be more than sent, and a reason
     * is required when goods are damaged or missing. The status becomes RECEIVED either way; shortfalls stay on
     * the lines. Everything is checked before anything changes.
     */
    public void receive(UUID userId, List<TransferReceiptLine> lines) {
        requireStatus(TransferStatus.SUBMITTED, "Only a SUBMITTED transfer can be received");
        Map<UUID, TransferReceiptLine> byProduct = new HashMap<>();
        for (TransferReceiptLine line : lines == null ? List.<TransferReceiptLine>of() : lines) {
            if (line == null || line.productId() == null || itemFor(line.productId()).isEmpty()) {
                throw new InvalidTransferException("A receipt line refers to a product that is not on this transfer");
            }
            if (byProduct.put(line.productId(), line) != null) {
                throw new InvalidTransferException("Product " + line.productId() + " appears twice in the receipt");
            }
        }
        Map<UUID, String> reasons = new HashMap<>();
        for (TransferItem item : items) {
            TransferReceiptLine line = byProduct.get(item.getProductId());
            if (line == null) {
                throw new InvalidTransferException("The receipt is missing product " + item.getProductId());
            }
            reasons.put(item.getProductId(),
                    item.checkReceipt(line.receivedQuantity(), line.damagedQuantity(), line.discrepancyReason()));
        }
        for (TransferItem item : items) {
            TransferReceiptLine line = byProduct.get(item.getProductId());
            item.applyReceipt(line.receivedQuantity(), line.damagedQuantity(), reasons.get(item.getProductId()));
        }
        TransferStatus from = transferStatus;
        this.transferStatus = TransferStatus.RECEIVED;
        this.hasDiscrepancy = items.stream().anyMatch(TransferItem::hasDiscrepancy);
        this.receivedBy = userId;
        this.receivedAt = Instant.now();
        touch();
        record(TransferEventType.RECEIVED, from, transferStatus, userId, null);
    }

    /** Calls the transfer off before receipt. The application service returns the goods to the source. */
    public void cancel(UUID userId, String reason) {
        if (transferStatus != TransferStatus.SUBMITTED) {
            throw new InvalidTransferStateException(
                    "A " + transferStatus + " transfer is final and cannot be cancelled");
        }
        String cleaned = reason == null ? "" : reason.trim();
        if (cleaned.length() < MIN_REASON_LENGTH || cleaned.length() > MAX_REASON_LENGTH) {
            throw new InvalidTransferException("A cancel reason of " + MIN_REASON_LENGTH + " to "
                    + MAX_REASON_LENGTH + " characters is required");
        }
        TransferStatus from = transferStatus;
        this.transferStatus = TransferStatus.CANCELLED;
        this.cancelledBy = userId;
        this.cancelReason = cleaned;
        this.cancelledAt = Instant.now();
        touch();
        record(TransferEventType.CANCELLED, from, transferStatus, userId, cleaned);
    }

    // ------------------------------------------------------------------ queries

    public Optional<TransferItem> itemFor(UUID productId) {
        return items.stream().filter(i -> i.getProductId().equals(productId)).findFirst();
    }

    /** Goods that left the source and have not been confirmed yet. */
    public boolean isInTransit() {
        return transferStatus == TransferStatus.SUBMITTED;
    }

    /** Received, with at least one line damaged or short. */
    public boolean hasDiscrepancy() {
        return hasDiscrepancy;
    }

    /** Base quantity of a quantity of one line: quantity x the line's conversion factor. */
    public long baseQuantity(UUID productId, int quantity) {
        return itemFor(productId)
                .orElseThrow(() -> new InvalidTransferException("Product " + productId + " is not on this transfer"))
                .baseQuantity(quantity);
    }

    public List<TransferItem> getItems() { return Collections.unmodifiableList(items); }

    public List<TransferEvent> pendingEvents() { return List.copyOf(pendingEvents); }

    public void clearPendingEvents() { pendingEvents.clear(); }

    public UUID getTransferId() { return transferId; }
    public TransferNumber getTransferNumber() { return transferNumber; }
    public UUID getSourceLocationId() { return sourceLocationId; }
    public UUID getDestinationLocationId() { return destinationLocationId; }
    public TransferStatus getTransferStatus() { return transferStatus; }
    public String getNote() { return note; }
    public UUID getCreatedBy() { return createdBy; }
    public UUID getReceivedBy() { return receivedBy; }
    public UUID getCancelledBy() { return cancelledBy; }
    public String getCancelReason() { return cancelReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReceivedAt() { return receivedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ------------------------------------------------------------------ helpers

    private void requireStatus(TransferStatus expected, String message) {
        if (transferStatus != expected) {
            throw new InvalidTransferStateException(message + " (this one is " + transferStatus + ")");
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private void record(TransferEventType type, TransferStatus from, TransferStatus to, UUID by, String eventNote) {
        pendingEvents.add(TransferEvent.record(transferId, type, from, to, by, eventNote));
    }

    private static void validateItems(List<TransferItem> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidTransferException("A transfer needs at least one line");
        }
        if (items.size() > MAX_ITEMS) {
            throw new InvalidTransferException("A transfer has at most " + MAX_ITEMS + " lines");
        }
        Set<UUID> seen = new HashSet<>();
        for (TransferItem item : items) {
            if (item == null) {
                throw new InvalidTransferException("A line cannot be empty");
            }
            if (!seen.add(item.getProductId())) {
                throw new DuplicateTransferItemException(item.getProductId());
            }
        }
    }

    private static String cleanNote(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String cleaned = value.trim();
        if (cleaned.length() > MAX_NOTE_LENGTH) {
            throw new InvalidTransferException("Note must be at most " + MAX_NOTE_LENGTH + " characters");
        }
        return cleaned;
    }
}