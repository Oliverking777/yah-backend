package tsgp.yah_enterprise.purchasing.application.dto;

import tsgp.yah_enterprise.purchasing.domain.model.RejectionType;

import java.util.UUID;

/**
 * What arrived for one purchase line, in the line's unit. accepted goes into stock (at most what was ordered);
 * rejected does not. A rejection needs a type; a reason is required when the line differs from the order.
 */
public record DeliveryLineCommand(UUID purchaseItemId, int accepted, int rejected, RejectionType rejectionType,
                                  String reason) { }
