package tsgp.yah_enterprise.product.domain.port;

import java.util.UUID;

public record ProductCategory(UUID categoryId, String categoryName, boolean active) {}
