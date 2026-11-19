package tsgp.yah_enterprise.transfer.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** One step in the life of a transfer. Appended on every transition; never edited or deleted. */
public class TransferEvent {

    private final UUID eventId;
    private final UUID transferId;
    private final TransferEventType eventType;
    private final TransferStatus fromStatus;
    private final TransferStatus toStatus;
    private final UUID performedBy;
    private final Instant occurredAt;
    private final String note;

    private TransferEvent(UUID eventId, UUID transferId, TransferEventType eventType, TransferStatus fromStatus,
                          TransferStatus toStatus, UUID performedBy, Instant occurredAt, String note) {
        this.eventId = Objects.requireNonNull(eventId);
        this.transferId = Objects.requireNonNull(transferId);
        this.eventType = Objects.requireNonNull(eventType);
        this.fromStatus = fromStatus;
        this.toStatus = Objects.requireNonNull(toStatus);
        this.performedBy = Objects.requireNonNull(performedBy);
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.note = note;
    }

    static TransferEvent record(UUID transferId, TransferEventType type, TransferStatus from, TransferStatus to,
                                UUID performedBy, String note) {
        return new TransferEvent(UUID.randomUUID(), transferId, type, from, to, performedBy, Instant.now(), note);
    }

    public static TransferEvent reconstitute(UUID eventId, UUID transferId, TransferEventType eventType,
                                             TransferStatus fromStatus, TransferStatus toStatus, UUID performedBy,
                                             Instant occurredAt, String note) {
        return new TransferEvent(eventId, transferId, eventType, fromStatus, toStatus, performedBy, occurredAt, note);
    }

    public UUID getEventId() { return eventId; }
    public UUID getTransferId() { return transferId; }
    public TransferEventType getEventType() { return eventType; }
    public TransferStatus getFromStatus() { return fromStatus; }
    public TransferStatus getToStatus() { return toStatus; }
    public UUID getPerformedBy() { return performedBy; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getNote() { return note; }
}
