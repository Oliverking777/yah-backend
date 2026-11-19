package tsgp.yah_enterprise.product.application.port.in;

import tsgp.yah_enterprise.product.application.dto.VariantUnitResult;
import java.util.List;
import java.util.UUID;

public interface ListVariantUnitsUseCase { List<VariantUnitResult> listVariantUnits(UUID variantId); }
