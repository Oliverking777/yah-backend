package tsgp.yah_enterprise.sales.application.dto;

import java.util.UUID;

/** Who is acting. anyLocation is true for ADMIN, OWNER and MANAGER (roles that work across every location). */
public record OrderActor(UUID userId, boolean anyLocation) { }
