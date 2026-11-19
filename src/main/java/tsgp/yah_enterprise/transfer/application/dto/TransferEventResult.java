package tsgp.yah_enterprise.transfer.application.dto;

import tsgp.yah_enterprise.transfer.domain.model.TransferEvent;

import java.time.Instant;
import java.util.UUID;

public record TransferEventResult(UUID eventId, String eventType, String fromStatus, String toStatus,
                                  UUID performedBy, Instant occurredAt, String note) {

    public static TransferEventResult from(TransferEvent e) {
        return new TransferEventResult(e.getEventId(), e.getEventType().name(),
                e.getFromStatus() == null ? null : e.getFromStatus().name(), e.getToStatus().name(),
                e.getPerformedBy(), e.getOccurredAt(), e.getNote());
    }
}
