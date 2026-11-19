package tsgp.yah_enterprise.pricing.application.dto;

import java.util.UUID;

/**
 * The price that applies to a sale line. Sales takes the figures from here,
 * never from the client.
 */
public record ApplicablePriceResult(
        UUID priceId,
        long unitPrice,
        int quantity,
        long lineTotal
) {
}
