package tsgp.yah_enterprise.sales.application.dto;

import java.util.List;
import java.util.UUID;

public record ValidateOrderCommand(UUID orderId, List<DiscountCommand> discounts, OrderActor actor) { }
