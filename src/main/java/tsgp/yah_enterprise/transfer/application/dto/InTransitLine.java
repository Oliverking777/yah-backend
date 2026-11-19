package tsgp.yah_enterprise.transfer.application.dto;

import java.util.UUID;

/** The base quantity of one product that has left the source and is still travelling to one destination. */
public record InTransitLine(UUID destinationLocationId, String destinationLocationName,
                            UUID productId, String productName, long baseQuantity) { }
