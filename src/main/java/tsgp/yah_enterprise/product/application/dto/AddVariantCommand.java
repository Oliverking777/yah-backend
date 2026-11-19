package tsgp.yah_enterprise.product.application.dto;

import java.util.UUID;

/** A new named variant of a product, e.g. "25 kg 10% broken", with its units. Its SKU is generated. */
public record AddVariantCommand(UUID productId, NewVariantCommand variant) {}
