package tsgp.yah_enterprise.dashboard.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** alertKey is TYPE:entityType:entityId. resolvedAt is always null in v1: only current alerts exist. */
public record OperationalAlert(String alertKey, AlertType alertType, UUID locationId, String entityType, UUID entityId, AlertSeverity severity, String message, Instant createdAt, Instant resolvedAt) { }
