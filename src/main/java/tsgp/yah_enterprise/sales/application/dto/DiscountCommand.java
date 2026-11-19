package tsgp.yah_enterprise.sales.application.dto;

import java.util.UUID;

public record DiscountCommand(UUID productId, UUID unitId, long discountAmount) { }
