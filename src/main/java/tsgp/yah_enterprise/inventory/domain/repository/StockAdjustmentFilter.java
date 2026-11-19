package tsgp.yah_enterprise.inventory.domain.repository;

import tsgp.yah_enterprise.inventory.domain.model.AdjustmentStatus;

import java.util.UUID;

public record StockAdjustmentFilter(AdjustmentStatus status, UUID locationId, UUID productId) {
}
