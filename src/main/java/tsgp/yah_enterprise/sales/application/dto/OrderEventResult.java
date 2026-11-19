package tsgp.yah_enterprise.sales.application.dto;

import java.time.Instant;
import java.util.UUID;

public record OrderEventResult(UUID eventId, String eventType, String fromStatus, String toStatus,
                               UUID performedBy, Instant occurredAt, String note) { }
