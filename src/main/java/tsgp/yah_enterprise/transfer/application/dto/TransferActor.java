package tsgp.yah_enterprise.transfer.application.dto;

import java.util.UUID;

/**
 * Who is acting. anyLocation is true for users whose role works across every location (ADMIN, OWNER):
 * they may act on any location. Everyone else must be assigned to the location the action needs.
 */
public record TransferActor(UUID userId, boolean anyLocation) { }
