package tsgp.yah_enterprise.sales.application.dto;

import java.util.UUID;

public record OrderItemResult(UUID orderItemId, UUID productId, UUID unitId, int conversionFactor, int quantity,
                              long baseQuantity, Long unitPrice, UUID priceId, long discountAmount,
                              long lineTotal) { }
