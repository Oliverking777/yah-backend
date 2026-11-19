package tsgp.yah_enterprise.product.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.product.application.dto.*;
import tsgp.yah_enterprise.product.application.port.in.*;
import tsgp.yah_enterprise.product.domain.exception.*;
import tsgp.yah_enterprise.product.domain.model.NewVariant;
import tsgp.yah_enterprise.product.domain.model.Product;
import tsgp.yah_enterprise.product.domain.model.ProductVariant;
import tsgp.yah_enterprise.product.domain.model.VariantNames;
import tsgp.yah_enterprise.product.domain.model.VariantUnit;
import tsgp.yah_enterprise.product.domain.port.CategoryDirectoryPort;
import tsgp.yah_enterprise.product.domain.port.ProductCodeGenerator;
import tsgp.yah_enterprise.product.domain.port.ProductAuditPort;
import tsgp.yah_enterprise.product.domain.port.ProductUnitInfo;
import tsgp.yah_enterprise.product.domain.port.UnitDirectoryPort;
import tsgp.yah_enterprise.product.domain.repository.ProductRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Products and their variants. What is stocked and sold is a variant; units and conversions belong to the variant.
 * Product codes (PRD-000001) and SKUs (PRD-000001-01) are generated, never typed.
 * Unit operations and conversions take a variant id. A module still keyed by product id may pass it: the first
 * variant of every product has the product's id.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService implements
        CreateProductUseCase, GetProductUseCase, ListProductsUseCase,
        UpdateProductUseCase, ChangeProductCategoryUseCase, ChangeProductStatusUseCase,
        AddVariantUseCase, UpdateVariantUseCase, ChangeVariantStatusUseCase, GetVariantsUseCase,
        AssignUnitToVariantUseCase, DefineVariantConversionUseCase,
        RemoveUnitFromVariantUseCase, DeactivateVariantUnitUseCase,
        ListVariantUnitsUseCase, ConvertQuantityUseCase {

    private static final String PRODUCT = "PRODUCT";
    private static final String VARIANT = "PRODUCT_VARIANT";
    /** Codes come from a sequence; only an old free-form code equal to a generated one can make a few draws fail. */
    private static final int MAX_CODE_ATTEMPTS = 50;

    private final ProductRepository productRepository;
    private final ProductAuditPort audit;
    private final CategoryDirectoryPort categoryDirectoryPort;
    private final UnitDirectoryPort unitDirectoryPort;
    private final ProductCodeGenerator productCodeGenerator;

    // ------------------------------------------------------------------ products

    @Override
    public ProductResult createProduct(CreateProductCommand cmd) {
        ensureCategoryAssignable(cmd.categoryId());
        ensureUnitsAssignable(cmd.variants());

        Product product = Product.create(nextFreeCode(), cmd.productName(), cmd.description(),
                cmd.productImageUrl(), cmd.categoryId(), cmd.variants().stream().map(ProductService::toDraft).toList(),
                productRepository::existsBySku);
        return toResult(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResult getProduct(UUID productId) {
        return toResult(find(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageResult listProducts(ProductPageRequest req) {
        boolean searching = req.search() != null && !req.search().isBlank();
        List<Product> products = searching
                ? productRepository.search(req.search().trim(), req.page(), req.size())
                : productRepository.findAll(req.page(), req.size(), req.status());
        long total = searching
                ? productRepository.countSearch(req.search().trim())
                : productRepository.count(req.status());
        return ProductPageResult.of(toResults(products), req.page(), req.size(), total);
    }

    @Override
    public ProductResult updateProduct(UUID productId, UpdateProductCommand cmd) {
        Product product = find(productId);
        if (cmd.productName() != null) product.changeName(cmd.productName());
        if (cmd.description() != null) product.changeDescription(cmd.description());
        if (cmd.productImageUrl() != null) product.changeImageUrl(cmd.productImageUrl());
        return toResult(productRepository.save(product));
    }

    @Override
    public ProductResult changeProductCategory(UUID productId, UUID categoryId) {
        Product product = find(productId);
        ensureCategoryAssignable(categoryId);
        UUID previousCategoryId = product.getCategoryId();
        product.changeCategory(categoryId);
        Product saved = productRepository.save(product);
        record("PRODUCT.CATEGORY_CHANGED", PRODUCT, productId,
                change("categoryId", previousCategoryId, categoryId));
        return toResult(saved);
    }

    @Override
    public ProductResult activateProduct(UUID productId) {
        Product product = find(productId);
        product.activate();
        Product saved = productRepository.save(product);
        record("PRODUCT.STATUS_CHANGED", PRODUCT, productId, change("productStatus", "INACTIVE", "ACTIVE"));
        return toResult(saved);
    }

    @Override
    public ProductResult deactivateProduct(UUID productId) {
        Product product = find(productId);
        product.deactivate();
        Product saved = productRepository.save(product);
        record("PRODUCT.STATUS_CHANGED", PRODUCT, productId, change("productStatus", "ACTIVE", "INACTIVE"));
        return toResult(saved);
    }

    // ------------------------------------------------------------------ variants

    @Override
    public ProductResult addVariant(AddVariantCommand cmd) {
        Product product = find(cmd.productId());
        ensureUnitsAssignable(List.of(cmd.variant()));
        ProductVariant variant = product.addVariant(toDraft(cmd.variant()), productRepository::existsBySku);
        Product saved = productRepository.save(product);
        record("PRODUCT.VARIANT_ADDED", VARIANT, variant.getVariantId(),
                change("variantName", null, variant.getVariantName().value()),
                change("sku", null, variant.getSku().value()));
        return toResult(saved);
    }

    @Override
    public ProductResult updateVariant(UUID variantId, UpdateVariantCommand cmd) {
        Product product = findByVariant(variantId);
        ProductVariant variant = product.findVariant(variantId);
        List<ProductAuditPort.Change> changes = new ArrayList<>();
        if (cmd.variantName() != null) {
            String before = variant.getVariantName() == null ? null : variant.getVariantName().value();
            product.renameVariant(variantId, cmd.variantName());
            String after = variant.getVariantName() == null ? null : variant.getVariantName().value();
            if (!Objects.equals(before, after)) changes.add(change("variantName", before, after));
        }
        Product saved = productRepository.save(product);
        if (!changes.isEmpty()) {
            record("PRODUCT.VARIANT_UPDATED", VARIANT, variantId, changes.toArray(ProductAuditPort.Change[]::new));
        }
        return toResult(saved);
    }

    @Override
    public ProductResult activateVariant(UUID variantId) {
        Product product = findByVariant(variantId);
        product.activateVariant(variantId);
        Product saved = productRepository.save(product);
        record("PRODUCT.VARIANT_STATUS_CHANGED", VARIANT, variantId, change("variantStatus", "INACTIVE", "ACTIVE"));
        return toResult(saved);
    }

    @Override
    public ProductResult deactivateVariant(UUID variantId) {
        Product product = findByVariant(variantId);
        product.deactivateVariant(variantId);
        Product saved = productRepository.save(product);
        record("PRODUCT.VARIANT_STATUS_CHANGED", VARIANT, variantId, change("variantStatus", "ACTIVE", "INACTIVE"));
        return toResult(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariantSummary> getVariants(Collection<UUID> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return List.of();
        Set<UUID> wanted = new HashSet<>(variantIds);
        List<Product> products = productRepository.findAllByVariantIds(wanted);
        Map<UUID, ProductUnitInfo> units = unitInfo(products);
        List<VariantSummary> out = new ArrayList<>();
        for (Product product : products) {
            for (ProductVariant variant : product.getVariants()) {
                if (wanted.contains(variant.getVariantId())) out.add(toSummary(product, variant, units));
            }
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VariantSummary> findVariantBySku(String sku) {
        if (sku == null || sku.isBlank()) return Optional.empty();
        String code = sku.trim();
        return productRepository.findBySku(code).flatMap(product -> {
            Map<UUID, ProductUnitInfo> units = unitInfo(List.of(product));
            return product.getVariants().stream()
                    .filter(v -> v.getSku().value().equalsIgnoreCase(code))
                    .findFirst()
                    .map(v -> toSummary(product, v, units));
        });
    }

    // ------------------------------------------------------------- variant units

    @Override
    public VariantUnitResult assignUnit(AssignUnitToVariantCommand cmd) {
        Product product = findByVariant(cmd.variantId());
        ensureUnitAssignable(cmd.unitId());
        product.addUnit(cmd.variantId(), cmd.unitId(), cmd.conversionFactor());
        Product saved = productRepository.save(product);
        return toUnitResult(saved.findVariant(cmd.variantId()).findUnit(cmd.unitId()));
    }

    @Override
    public VariantUnitResult defineConversion(DefineVariantConversionCommand cmd) {
        Product product = findByVariant(cmd.variantId());
        product.changeConversion(cmd.variantId(), cmd.unitId(), cmd.conversionFactor());
        Product saved = productRepository.save(product);
        return toUnitResult(saved.findVariant(cmd.variantId()).findUnit(cmd.unitId()));
    }

    @Override
    public void removeUnit(UUID variantId, UUID unitId) {
        Product product = findByVariant(variantId);
        product.removeUnit(variantId, unitId);
        productRepository.save(product);
    }

    @Override
    public VariantUnitResult deactivateUnit(UUID variantId, UUID unitId) {
        Product product = findByVariant(variantId);
        product.deactivateUnit(variantId, unitId);
        Product saved = productRepository.save(product);
        return toUnitResult(saved.findVariant(variantId).findUnit(unitId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariantUnitResult> listVariantUnits(UUID variantId) {
        Product product = findByVariant(variantId);
        Map<UUID, ProductUnitInfo> units = unitInfo(List.of(product));
        return product.findVariant(variantId).getUnits().stream().map(u -> toUnitResult(u, units)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long convertQuantity(UUID variantId, UUID fromUnitId, UUID toUnitId, long quantity) {
        return findByVariant(variantId).convert(variantId, fromUnitId, toUnitId, quantity);
    }

    // ------------------------------------------------------------------- helpers

    private Product find(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Product findByVariant(UUID variantId) {
        return productRepository.findByVariantId(variantId)
                .orElseThrow(() -> new VariantNotFoundException(variantId));
    }

    private void ensureCategoryAssignable(UUID categoryId) {
        categoryDirectoryPort.findById(categoryId)
                .filter(c -> c.active())
                .orElseThrow(() -> new CategoryNotAssignableException(categoryId));
    }

    /** Every unit of these variants (base units included) must exist and be active; one lookup for all. */
    private void ensureUnitsAssignable(List<NewVariantCommand> variants) {
        Set<UUID> unitIds = new LinkedHashSet<>();
        for (NewVariantCommand v : variants) {
            if (v.baseUnitId() == null) throw new InvalidProductException("Every variant needs a base unit");
            unitIds.add(v.baseUnitId());
            for (NewVariantCommand.Unit u : v.units()) {
                if (u.unitId() == null) throw new InvalidProductException("Every unit of a variant needs a unit id");
                unitIds.add(u.unitId());
            }
        }
        Map<UUID, ProductUnitInfo> found = unitDirectoryPort.findAllByIds(unitIds).stream()
                .collect(Collectors.toMap(ProductUnitInfo::unitId, Function.identity(), (a, b) -> a));
        for (UUID unitId : unitIds) {
            ProductUnitInfo info = found.get(unitId);
            if (info == null || !info.active()) throw new UnitNotAssignableException(unitId);
        }
    }

    private void ensureUnitAssignable(UUID unitId) {
        unitDirectoryPort.findById(unitId)
                .filter(ProductUnitInfo::active)
                .orElseThrow(() -> new UnitNotAssignableException(unitId));
    }

    /** A code no product has yet: the sequence never repeats, but an old typed code may equal a generated one. */
    private String nextFreeCode() {
        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            String code = productCodeGenerator.nextCode();
            if (!productRepository.existsByCode(code)) return code;
        }
        throw new IllegalStateException("No free product code after " + MAX_CODE_ATTEMPTS + " attempts");
    }

    private static NewVariant toDraft(NewVariantCommand v) {
        return new NewVariant(v.variantName(), v.baseUnitId(),
                v.units().stream().map(u -> new NewVariant.NewUnit(u.unitId(), u.conversionFactor())).toList());
    }

    private void record(String action, String entityType, UUID entityId, ProductAuditPort.Change... changes) {
        audit.record(new ProductAuditPort.AuditRecord(null, action, entityType, entityId, null, null,
                List.of(changes)));
    }

    private static ProductAuditPort.Change change(String field, Object before, Object after) {
        return new ProductAuditPort.Change(field, before == null ? null : String.valueOf(before),
                after == null ? null : String.valueOf(after));
    }

    // ---- results ----

    private ProductResult toResult(Product product) {
        return toResults(List.of(product)).get(0);
    }

    /** One unit lookup for the whole list. */
    private List<ProductResult> toResults(List<Product> products) {
        Map<UUID, ProductUnitInfo> units = unitInfo(products);
        return products.stream().map(p -> new ProductResult(p.getProductId(), p.getProductCode().value(),
                p.getProductName().value(), p.getDescription(), p.getProductImageUrl().value(), p.getCategoryId(),
                p.getProductStatus().name(), p.isSimple(),
                p.getVariants().stream().map(v -> toVariantResult(p, v, units)).toList(),
                p.getCreatedAt(), p.getUpdatedAt())).toList();
    }

    private VariantResult toVariantResult(Product product, ProductVariant v, Map<UUID, ProductUnitInfo> units) {
        return new VariantResult(v.getVariantId(), v.getVariantName() == null ? null : v.getVariantName().value(),
                VariantNames.fullName(product, v), v.getSku().value(), v.getVariantStatus().name(),
                v.getUnits().stream().map(u -> toUnitResult(u, units)).toList(), v.getCreatedAt(), v.getUpdatedAt());
    }

    private VariantSummary toSummary(Product product, ProductVariant v, Map<UUID, ProductUnitInfo> units) {
        return new VariantSummary(v.getVariantId(), product.getProductId(), product.getProductCode().value(),
                product.getProductName().value(), v.getVariantName() == null ? null : v.getVariantName().value(),
                VariantNames.fullName(product, v), v.getSku().value(), product.isActive() && v.isActive(),
                v.findBaseUnit().map(VariantUnit::getUnitId).orElse(null),
                v.getUnits().stream().map(u -> toUnitResult(u, units)).toList());
    }

    private Map<UUID, ProductUnitInfo> unitInfo(List<Product> products) {
        Set<UUID> unitIds = products.stream()
                .flatMap(p -> p.getVariants().stream())
                .flatMap(v -> v.getUnits().stream())
                .map(VariantUnit::getUnitId)
                .collect(Collectors.toSet());
        if (unitIds.isEmpty()) return Map.of();
        return unitDirectoryPort.findAllByIds(unitIds).stream()
                .collect(Collectors.toMap(ProductUnitInfo::unitId, Function.identity(), (a, b) -> a));
    }

    private VariantUnitResult toUnitResult(VariantUnit unit) {
        return toUnitResult(unit, unitDirectoryPort.findById(unit.getUnitId()).orElse(null));
    }

    private static VariantUnitResult toUnitResult(VariantUnit unit, Map<UUID, ProductUnitInfo> units) {
        return toUnitResult(unit, units.get(unit.getUnitId()));
    }

    private static VariantUnitResult toUnitResult(VariantUnit unit, ProductUnitInfo info) {
        return VariantUnitResult.from(unit, info == null ? null : info.unitCode(), info == null ? null : info.unitName());
    }
}