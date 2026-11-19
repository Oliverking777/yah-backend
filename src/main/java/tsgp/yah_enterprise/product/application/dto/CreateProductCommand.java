package tsgp.yah_enterprise.product.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * A new product with all its variants and their units. The product code and the SKUs are generated.
 * One variant without a name makes a simple product; with several, each needs a name.
 */
public record CreateProductCommand(
        String productName, String description, String productImageUrl, UUID categoryId,
        List<NewVariantCommand> variants) {

    public CreateProductCommand {
        variants = variants == null ? List.of() : List.copyOf(variants);
    }
}