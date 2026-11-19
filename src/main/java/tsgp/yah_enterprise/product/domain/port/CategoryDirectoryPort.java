package tsgp.yah_enterprise.product.domain.port;

import java.util.Optional;
import java.util.UUID;

public interface CategoryDirectoryPort {
    Optional<ProductCategory> findById(UUID categoryId);
}
