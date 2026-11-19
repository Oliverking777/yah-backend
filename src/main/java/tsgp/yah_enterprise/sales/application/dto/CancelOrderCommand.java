package tsgp.yah_enterprise.sales.application.dto;

import java.util.UUID;

/** canCancelAny is true for a holder of the validate permission; everyone else cancels only their own order. */
public record CancelOrderCommand(UUID orderId, String reason, OrderActor actor, boolean canCancelAny) { }
