package tsgp.yah_enterprise.transfer.presentation.dto;

import tsgp.yah_enterprise.transfer.application.dto.TransferEventResult;

import java.time.Instant;
import java.util.UUID;

public record TransferEventResponse(UUID eventId, String eventType, String fromStatus, String toStatus,
                                    UUID performedBy, Instant occurredAt, String note) {

    public static TransferEventResponse from(TransferEventResult r) {
        return new TransferEventResponse(r.eventId(), r.eventType(), r.fromStatus(), r.toStatus(),
                r.performedBy(), r.occurredAt(), r.note());
    }
}
