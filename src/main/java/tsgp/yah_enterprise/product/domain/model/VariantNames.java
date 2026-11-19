package tsgp.yah_enterprise.product.domain.model;

/**
 * The name shown for a variant everywhere (lines, stock, reports): "Indian Rice · 50 kg 10% broken", or just
 * "Indian Rice" for the default variant of a simple product.
 */
public final class VariantNames {

    public static final String SEPARATOR = " · ";

    private VariantNames() { }

    public static String fullName(Product product, ProductVariant variant) {
        String productName = product.getProductName().value();
        return variant.isDefault() ? productName : productName + SEPARATOR + variant.getVariantName().value();
    }
}