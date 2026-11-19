package tsgp.yah_enterprise.product.application.port.in;

import tsgp.yah_enterprise.product.application.dto.*;
import java.util.UUID;

/** Renames a variant. Answers the whole product. */
public interface UpdateVariantUseCase { ProductResult updateVariant(UUID variantId, UpdateVariantCommand command); }
