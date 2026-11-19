package tsgp.yah_enterprise.transfer.domain.repository;

import java.util.UUID;

/** Base units sent to a location (SUBMITTED transfers) and not yet received. */
public record InTransitRow(UUID destinationLocationId, UUID productId, long quantity) { }
