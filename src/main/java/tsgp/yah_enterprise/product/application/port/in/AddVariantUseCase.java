package tsgp.yah_enterprise.product.application.port.in;

import tsgp.yah_enterprise.product.application.dto.*;

/** Adds a named variant with its units; its SKU is generated. A simple product's unnamed variant must be named first (UpdateVariantUseCase). */
public interface AddVariantUseCase { ProductResult addVariant(AddVariantCommand command); }
