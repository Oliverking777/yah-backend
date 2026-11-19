package tsgp.yah_enterprise.audit.application.dto;

import java.util.UUID;

/** Who is asking, from which device, under which correlation id. Built by the controller, never by the client body. */
public record AuditRequestContext(UUID userId, String deviceId, UUID correlationId) { }
