package tsgp.yah_enterprise.product.domain.port;

import java.util.UUID;

public record ProductUnitInfo(UUID unitId, String unitCode, String unitName, boolean active) {}
