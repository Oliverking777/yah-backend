package tsgp.yah_enterprise.product.application.dto;

/**
 * Renames a variant. Empty removes the name, which is allowed only while it is the product's only variant.
 * The SKU never changes.
 */
public record UpdateVariantCommand(String variantName) {}
