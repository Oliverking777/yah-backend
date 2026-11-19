package tsgp.yah_enterprise.product.domain.model;

import lombok.Getter;
import tsgp.yah_enterprise.product.domain.exception.*;
import tsgp.yah_enterprise.product.domain.valueobject.ProductCode;
import tsgp.yah_enterprise.product.domain.valueobject.ProductImageUrl;
import tsgp.yah_enterprise.product.domain.valueobject.ProductName;
import tsgp.yah_enterprise.product.domain.valueobject.VariantName;
import tsgp.yah_enterprise.product.domain.valueobject.VariantSku;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The commercial item, e.g. Indian Rice. Aggregate root of its variants: what is stocked and sold is always a
 * {@link ProductVariant}, never the product itself. Variants are named within the product: Indian Rice has
 * "50 kg 10% broken", "25 kg 10% broken", "50 kg 5% broken"; Flour has "50 kg" and "25 kg". The packaging (bag,
 * carton) is the variant's unit, not part of its name. A simple product has one default variant without a name.
 * <p>
 * Rules kept here, because they need every variant:
 * <ul>
 *   <li>at least one variant, created with the product (several may be created at once, each with its units);</li>
 *   <li>names unique within the product, ignoring case and spaces;</li>
 *   <li>a variant without a name only while it is the product's only variant, so every variant of a product with
 *       several can be told apart;</li>
 *   <li>SKUs generated from the product code: PRD-000001-01, PRD-000001-02... unique within the product, and
 *       across products through the {@code skuInUse} check the service passes in;</li>
 *   <li>an active product keeps at least one active variant (deactivate the product instead).</li>
 * </ul>
 */
@Getter
public class Product {

    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_SKU_LENGTH = 30;
    /** Enough room for any product; stops the search should every candidate somehow be taken. */
    private static final int MAX_SKU_NUMBER = 9999;

    private final UUID productId;
    private final ProductCode productCode;
    private ProductName productName;
    private String description;
    private ProductImageUrl productImageUrl;
    private UUID categoryId;
    private ProductStatus productStatus;
    private final List<ProductVariant> variants;
    private final Instant createdAt;
    private Instant updatedAt;

    private Product(UUID productId, ProductCode productCode, ProductName productName,
                    String description, ProductImageUrl productImageUrl, UUID categoryId,
                    ProductStatus productStatus, List<ProductVariant> variants,
                    Instant createdAt, Instant updatedAt) {
        this.productId = Objects.requireNonNull(productId);
        this.productCode = Objects.requireNonNull(productCode);
        this.productName = Objects.requireNonNull(productName);
        this.description = normaliseDescription(description);
        this.productImageUrl = Objects.requireNonNull(productImageUrl);
        this.categoryId = Objects.requireNonNull(categoryId);
        this.productStatus = Objects.requireNonNull(productStatus);
        if (variants == null || variants.isEmpty()) {
            throw new InvalidProductException("A product needs at least one variant");
        }
        this.variants = new ArrayList<>(variants);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    /**
     * A new active product with its variants and their units, all at once.
     * One unnamed variant makes a simple product; with several, each needs a name, unique within the product.
     *
     * @param code     the generated product code, e.g. PRD-000123
     * @param skuInUse true when another product already has this SKU (the variants' SKUs are generated from code)
     */
    public static Product create(String code, String name, String description, String imageUrl, UUID categoryId,
                                 List<NewVariant> newVariants, Predicate<String> skuInUse) {
        if (newVariants == null || newVariants.isEmpty()) {
            throw new InvalidProductException("A product needs at least one variant");
        }
        Instant now = Instant.now();
        UUID productId = UUID.randomUUID();
        ProductCode productCode = ProductCode.of(code);
        boolean several = newVariants.size() > 1;

        List<ProductVariant> variants = new ArrayList<>();
        Set<String> nameKeys = new HashSet<>();
        Set<String> skus = new HashSet<>();
        for (NewVariant draft : newVariants) {
            VariantName variantName = VariantName.ofNullable(draft.name());
            if (variantName == null && several) {
                throw new VariantNameRequiredException("Every variant of a product with several variants needs a name");
            }
            if (variantName != null && !nameKeys.add(variantName.key())) {
                throw new DuplicateVariantNameException(variantName.value());
            }
            VariantSku sku = nextSku(productCode, skus, skuInUse);
            skus.add(sku.value());
            // The first variant shares the product's id, like the variants the migration created for existing
            // products, so modules still keyed by product id keep pointing at the right variant while they move over.
            UUID variantId = variants.isEmpty() ? productId : UUID.randomUUID();
            variants.add(newVariant(variantId, sku, variantName, draft));
        }
        return new Product(productId, productCode, ProductName.of(name), description,
                ProductImageUrl.of(imageUrl), categoryId, ProductStatus.ACTIVE, variants, now, now);
    }

    public static Product reconstitute(UUID productId, String productCode, String productName,
                                       String description, String productImageUrl, UUID categoryId,
                                       ProductStatus productStatus, List<ProductVariant> variants,
                                       Instant createdAt, Instant updatedAt) {
        return new Product(productId, ProductCode.of(productCode), ProductName.of(productName),
                description, ProductImageUrl.of(productImageUrl), categoryId, productStatus,
                variants, createdAt, updatedAt);
    }

    /** In the order they were created. */
    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    /** True while the product has its single unnamed variant: screens show it as a plain product. */
    public boolean isSimple() {
        return variants.size() == 1 && variants.get(0).isDefault();
    }

    // ---- product ----

    public void changeName(String newName) {
        this.productName = ProductName.of(newName);
        touch();
    }

    public void changeDescription(String newDescription) {
        this.description = normaliseDescription(newDescription);
        touch();
    }

    public void changeImageUrl(String newImageUrl) {
        this.productImageUrl = ProductImageUrl.of(newImageUrl);
        touch();
    }

    public void changeCategory(UUID newCategoryId) {
        this.categoryId = Objects.requireNonNull(newCategoryId);
        touch();
    }

    /** Reactivating the product does not reactivate variants that were deactivated one by one. */
    public void activate() {
        if (productStatus == ProductStatus.ACTIVE) throw new ProductAlreadyActiveException(productId);
        if (variants.stream().noneMatch(ProductVariant::isActive)) {
            throw new InvalidProductException("Activate one of its variants first: the product has no active variant");
        }
        this.productStatus = ProductStatus.ACTIVE;
        touch();
    }

    /** Every variant becomes unusable with it, whatever its own status. */
    public void deactivate() {
        if (productStatus == ProductStatus.INACTIVE) throw new ProductAlreadyInactiveException(productId);
        this.productStatus = ProductStatus.INACTIVE;
        touch();
    }

    public void ensureActive() {
        if (productStatus != ProductStatus.ACTIVE) throw new ProductInactiveException(productId);
    }

    public boolean isActive() { return productStatus == ProductStatus.ACTIVE; }

    // ---- variants ----

    /**
     * Adds a named variant with its units; its SKU is generated. A simple product's unnamed variant is named first
     * ({@link #renameVariant}), so it keeps its stock and history and becomes one of the variants, e.g. "50 kg"
     * before "25 kg" is added.
     *
     * @param skuInUse true when another product already has this SKU
     */
    public ProductVariant addVariant(NewVariant draft, Predicate<String> skuInUse) {
        ensureActive();
        VariantName variantName = VariantName.ofNullable(draft.name());
        if (variantName == null) {
            throw new VariantNameRequiredException("A new variant needs a name");
        }
        variants.stream().filter(ProductVariant::isDefault).findFirst().ifPresent(v -> {
            throw new VariantNameRequiredException(
                    "Name the existing variant " + v.getSku() + " before adding another one");
        });
        ensureNameFree(variantName, null);
        Set<String> skus = variants.stream().map(v -> v.getSku().value()).collect(Collectors.toSet());
        ProductVariant variant = newVariant(UUID.randomUUID(), nextSku(productCode, skus, skuInUse), variantName, draft);
        variants.add(variant);
        touch();
        return variant;
    }

    /** Null or blank removes the name, which is only allowed while it is the product's only variant. */
    public void renameVariant(UUID variantId, String name) {
        ensureActive();
        ProductVariant variant = findVariant(variantId);
        VariantName variantName = VariantName.ofNullable(name);
        if (variantName == null && variants.size() > 1) {
            throw new VariantNameRequiredException("Every variant of a product with several variants needs a name");
        }
        ensureNameFree(variantName, variantId);
        variant.rename(variantName);
        touch();
    }

    public void activateVariant(UUID variantId) {
        findVariant(variantId).activate();
        touch();
    }

    /** The last active variant of an active product cannot be deactivated: deactivate the product instead. */
    public void deactivateVariant(UUID variantId) {
        ProductVariant variant = findVariant(variantId);
        long active = variants.stream().filter(ProductVariant::isActive).count();
        if (isActive() && variant.isActive() && active == 1) {
            throw new LastActiveVariantException(productId);
        }
        variant.deactivate();
        touch();
    }

    public ProductVariant findVariant(UUID variantId) {
        return variants.stream()
                .filter(v -> v.getVariantId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new VariantNotFoundException(variantId));
    }

    /** Checks that the variant may be bought, sold, transferred or counted now: product and variant active. */
    public ProductVariant ensureVariantUsable(UUID variantId) {
        ensureActive();
        ProductVariant variant = findVariant(variantId);
        variant.ensureActive();
        return variant;
    }

    // ---- units of a variant ----

    public void addUnit(UUID variantId, UUID unitId, int conversionFactor) {
        ensureActive();
        findVariant(variantId).addUnit(unitId, conversionFactor);
        touch();
    }

    public void changeConversion(UUID variantId, UUID unitId, int conversionFactor) {
        ensureActive();
        findVariant(variantId).changeConversion(unitId, conversionFactor);
        touch();
    }

    public void deactivateUnit(UUID variantId, UUID unitId) {
        ensureActive();
        findVariant(variantId).deactivateUnit(unitId);
        touch();
    }

    public void removeUnit(UUID variantId, UUID unitId) {
        findVariant(variantId).removeUnit(unitId);
        touch();
    }

    public long convert(UUID variantId, UUID fromUnitId, UUID toUnitId, long quantity) {
        return findVariant(variantId).convert(fromUnitId, toUnitId, quantity);
    }

    // ---- helpers ----

    private void ensureNameFree(VariantName name, UUID except) {
        if (name == null) return;
        boolean taken = variants.stream()
                .filter(v -> !v.getVariantId().equals(except))
                .anyMatch(v -> v.nameKey().equals(name.key()));
        if (taken) throw new DuplicateVariantNameException(name.value());
    }

    /** A variant with its base unit and its other units (each checked: factor 2 or more, once per variant). */
    private static ProductVariant newVariant(UUID variantId, VariantSku sku, VariantName name, NewVariant draft) {
        ProductVariant variant = ProductVariant.create(variantId, sku, name, draft.baseUnitId());
        for (NewVariant.NewUnit unit : draft.units()) {
            variant.addUnit(Objects.requireNonNull(unit.unitId(), "A unit is required"), unit.conversionFactor());
        }
        return variant;
    }

    /**
     * The next free SKU: the product code and a two-digit number, PRD-000123-01, -02... Numbering starts after the
     * variants the product already has. A long code (older products had free-form codes) is shortened so the SKU
     * stays within 30 characters.
     */
    private static VariantSku nextSku(ProductCode code, Set<String> taken, Predicate<String> skuInUse) {
        for (int n = taken.size() + 1; n <= MAX_SKU_NUMBER; n++) {
            String suffix = String.format("-%02d", n);
            String prefix = code.value();
            if (prefix.length() + suffix.length() > MAX_SKU_LENGTH) {
                prefix = prefix.substring(0, MAX_SKU_LENGTH - suffix.length());
            }
            String candidate = prefix + suffix;
            if (!taken.contains(candidate) && !skuInUse.test(candidate)) return VariantSku.of(candidate);
        }
        throw new VariantSkuAlreadyExistsException(code.value() + "-*");
    }

    private static String normaliseDescription(String description) {
        if (description == null) return null;
        String trimmed = description.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidProductException(
                    "Description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
        return trimmed;
    }

    private void touch() { this.updatedAt = Instant.now(); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Product p && productId.equals(p.productId));
    }

    @Override
    public int hashCode() { return productId.hashCode(); }
}