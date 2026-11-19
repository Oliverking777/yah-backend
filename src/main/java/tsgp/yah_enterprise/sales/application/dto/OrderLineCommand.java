package tsgp.yah_enterprise.sales.application.dto;

import java.util.UUID;

public record OrderLineCommand(UUID productId, UUID unitId, int quantity) { }
