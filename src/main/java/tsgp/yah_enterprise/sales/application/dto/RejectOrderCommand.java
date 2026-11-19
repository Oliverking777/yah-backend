package tsgp.yah_enterprise.sales.application.dto;

import java.util.UUID;

public record RejectOrderCommand(UUID orderId, String reason, OrderActor actor) { }
