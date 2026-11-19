package tsgp.yah_enterprise.product.application.port.in;

import tsgp.yah_enterprise.product.application.dto.VariantUnitResult;
import java.util.UUID;

public interface DeactivateVariantUnitUseCase { VariantUnitResult deactivateUnit(UUID variantId, UUID unitId); }
