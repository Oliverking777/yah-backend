package tsgp.yah_enterprise.product.domain.valueobject;

import tsgp.yah_enterprise.product.domain.exception.InvalidProductException;

import java.net.URI;
import java.net.URISyntaxException;

public record ProductImageUrl(String value) {

    public static final int MAX_LENGTH = 2048;

    public ProductImageUrl {
        if (value == null || value.isBlank()) {
            throw new InvalidProductException("Product image URL is required");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new InvalidProductException(
                    "Product image URL must not exceed " + MAX_LENGTH + " characters");
        }
        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException e) {
            throw new InvalidProductException("Product image URL is not a valid URL");
        }
        String scheme = uri.getScheme();
        boolean httpOrHttps = scheme != null
                && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
        if (!httpOrHttps || uri.getHost() == null) {
            throw new InvalidProductException("Product image URL must be a valid http or https URL");
        }
    }

    public static ProductImageUrl of(String value) { return new ProductImageUrl(value); }

    @Override
    public String toString() { return value; }
}