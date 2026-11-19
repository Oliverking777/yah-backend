package tsgp.yah_enterprise.product.application.port.in;

import java.util.UUID;

public interface RemoveUnitFromVariantUseCase { void removeUnit(UUID variantId, UUID unitId); }
