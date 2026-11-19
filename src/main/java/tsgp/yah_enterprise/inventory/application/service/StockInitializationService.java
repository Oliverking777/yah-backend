package tsgp.yah_enterprise.inventory.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import tsgp.yah_enterprise.inventory.application.dto.InitializeStockCommand;
import tsgp.yah_enterprise.inventory.application.dto.InitializeStockLineCommand;
import tsgp.yah_enterprise.inventory.application.dto.StockInitializationPageRequest;
import tsgp.yah_enterprise.inventory.application.dto.StockInitializationPageResult;
import tsgp.yah_enterprise.inventory.application.dto.StockInitializationItemResult;
import tsgp.yah_enterprise.inventory.application.dto.StockInitializationResult;
import tsgp.yah_enterprise.inventory.application.port.in.GetStockInitializationUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.InitializeStockUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.ListStockInitializationsUseCase;
import tsgp.yah_enterprise.inventory.domain.exception.DuplicateProductInInitializationException;
import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockInitializationException;
import tsgp.yah_enterprise.inventory.domain.exception.StockAlreadyExistsException;
import tsgp.yah_enterprise.inventory.domain.exception.StockAlreadyInitializedException;
import tsgp.yah_enterprise.inventory.domain.exception.StockInitializationNotFoundException;
import tsgp.yah_enterprise.inventory.domain.exception.StockLocationNotUsableException;
import tsgp.yah_enterprise.inventory.domain.exception.StockProductNotUsableException;
import tsgp.yah_enterprise.inventory.domain.model.MovementType;
import tsgp.yah_enterprise.inventory.domain.model.ReferenceType;
import tsgp.yah_enterprise.inventory.domain.model.StockInitialization;
import tsgp.yah_enterprise.inventory.domain.model.StockInitializationItem;
import tsgp.yah_enterprise.inventory.domain.model.StockInitializationStatus;
import tsgp.yah_enterprise.inventory.domain.port.InventoryAuditPort;
import tsgp.yah_enterprise.inventory.domain.port.InventoryLocation;
import tsgp.yah_enterprise.inventory.domain.port.InventoryLocationDirectoryPort;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProductDetail;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProduct;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProductCatalogPort;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProductUnit;
import tsgp.yah_enterprise.inventory.domain.repository.ProductLocation;
import tsgp.yah_enterprise.inventory.domain.repository.StockInitializationFilter;
import tsgp.yah_enterprise.inventory.domain.repository.StockInitializationRepository;
import tsgp.yah_enterprise.inventory.domain.repository.StockLevelRepository;
import tsgp.yah_enterprise.inventory.domain.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Stock initialization: the owner records the initial quantities of multiple products at one location
 * in a single atomic operation. No approval step — the initialization is COMPLETED on submission.
 * <p>
 * The service validates all business rules, converts counted quantities to base units, creates the
 * aggregate, posts one stock movement per line via {@link StockLedger} (joining this transaction),
 * and records an audit entry. If any validation or movement fails, the entire operation rolls back.
 * <p>
 * A product can only be initialized where it has no stock history: if any movement was ever recorded for it at
 * the location (an earlier initialization, a purchase, a transfer, a sale or an approved adjustment), the request
 * is refused and the quantity must be corrected with an adjustment. A stock level that only carries a low-stock
 * threshold has no history and does not block. The check runs with every line's stock level locked, so a
 * movement posted concurrently is either seen here or waits until this initialization has committed.
 */
@Service
@Transactional
public class StockInitializationService implements
        InitializeStockUseCase,
        GetStockInitializationUseCase,
        ListStockInitializationsUseCase {

    private final StockInitializationRepository initializationRepository;
    private final StockLedger stockLedger;
    private final StockAccessScope accessScope;
    private final InventoryProductCatalogPort productCatalog;
    private final InventoryLocationDirectoryPort locationDirectory;
    private final InventoryAuditPort audit;
    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;

    /** Kept for callers that do not audit (unit tests). */
    public StockInitializationService(StockInitializationRepository initializationRepository, StockLedger stockLedger,
                                      StockAccessScope accessScope, InventoryProductCatalogPort productCatalog,
                                      InventoryLocationDirectoryPort locationDirectory,
                                      StockLevelRepository stockLevelRepository,
                                      StockMovementRepository stockMovementRepository) {
        this(initializationRepository, stockLedger, accessScope, productCatalog, locationDirectory,
                stockLevelRepository, stockMovementRepository, InventoryAuditPort.NOOP);
    }

    @Autowired
    public StockInitializationService(StockInitializationRepository initializationRepository, StockLedger stockLedger,
                                      StockAccessScope accessScope, InventoryProductCatalogPort productCatalog,
                                      InventoryLocationDirectoryPort locationDirectory,
                                      StockLevelRepository stockLevelRepository,
                                      StockMovementRepository stockMovementRepository,
                                      InventoryAuditPort audit) {
        this.initializationRepository = initializationRepository;
        this.stockLedger = stockLedger;
        this.stockLevelRepository = stockLevelRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.accessScope = accessScope;
        this.productCatalog = productCatalog;
        this.locationDirectory = locationDirectory;
        this.audit = audit;
    }

    @Override
    public StockInitializationResult initializeStock(InitializeStockCommand command) {
        // 1. Access scope: a limited user may only initialize their own location
        accessScope.checkCanAct(command.locationId(), command.requestedBy(), command.restrictedToAssignedLocation());

        // 2. Validate the location exists and is active
        InventoryLocation location = locationDirectory.findLocation(command.locationId())
                .orElseThrow(() -> new StockLocationNotUsableException(command.locationId(), "it does not exist"));
        if (!location.active()) {
            throw new StockLocationNotUsableException(command.locationId(), "it is inactive");
        }

        // 3. Validate the items list
        if (command.items() == null || command.items().isEmpty()) {
            throw new InvalidStockInitializationException(
                    "A stock initialization must contain at least one product");
        }

        // 4. Check for duplicate products in the request
        Set<UUID> seenProductIds = new HashSet<>();
        for (InitializeStockLineCommand line : command.items()) {
            if (!seenProductIds.add(line.productId())) {
                throw new DuplicateProductInInitializationException(line.productId());
            }
        }

        // 5. Check that none of these products have already been initialized at this location
        Set<UUID> alreadyInitialized = initializationRepository.findInitializedProductIds(
                command.locationId(), seenProductIds);
        if (!alreadyInitialized.isEmpty()) {
            throw new StockAlreadyInitializedException(command.locationId(), alreadyInitialized.iterator().next());
        }

        // 6. Validate each product and unit, and build the domain items
        List<StockInitializationItem> domainItems = new ArrayList<>();
        Map<UUID, InventoryProductDetail> productDetails = new LinkedHashMap<>();

        for (InitializeStockLineCommand line : command.items()) {
            // Validate the product exists and is active
            InventoryProductDetail productDetail = productCatalog.findProductDetail(line.productId())
                    .orElseThrow(() -> new StockProductNotUsableException(line.productId(), "it does not exist"));
            if (!productDetail.active()) {
                throw new StockProductNotUsableException(line.productId(), "it is inactive");
            }
            productDetails.put(line.productId(), productDetail);

            // Validate the unit is available and active for this product
            InventoryProductUnit unit = productDetail.units().stream()
                    .filter(u -> u.unitId().equals(line.unitId()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidStockInitializationException(
                            "Unit " + line.unitId() + " is not available for product " + line.productId()));
            if (!unit.active()) {
                throw new InvalidStockInitializationException(
                        "Unit " + line.unitId() + " is inactive for product " + line.productId());
            }

            // Validate the counted quantity
            if (line.countedQuantity() < 1) {
                throw new InvalidStockInitializationException(
                        "Counted quantity for product " + line.productId() + " must be at least 1");
            }

            // Build the domain item (conversion to base units happens in the factory)
            domainItems.add(StockInitializationItem.create(
                    line.productId(), line.unitId(), unit.conversionFactor(), line.countedQuantity()));
        }

        // 7. Refuse products that already have stock history here. Lock every line's level first, in a fixed
        //    order so two initializations never deadlock each other; lockOrCreate may add a zero row, which is
        //    rolled back if this request fails and filled by step 9 if it succeeds.
        for (UUID productId : new TreeSet<>(seenProductIds)) {
            stockLevelRepository.lockOrCreate(productId, command.locationId());
        }
        Set<ProductLocation> withHistory = stockMovementRepository.findWithMovements(
                seenProductIds, Set.of(command.locationId()));
        if (!withHistory.isEmpty()) {
            // report the first offending line in request order, so the message is predictable
            UUID productId = command.items().stream().map(InitializeStockLineCommand::productId)
                    .filter(id -> withHistory.contains(new ProductLocation(id, command.locationId())))
                    .findFirst().orElseThrow();
            throw new StockAlreadyExistsException(command.locationId(), productId,
                    productDetails.get(productId).productName(), location.locationName());
        }

        // 8. Create the aggregate (factory checks no duplicates and non-empty)
        StockInitialization initialization = StockInitialization.create(
                initializationRepository.nextNumber(), command.locationId(), command.requestedBy(), domainItems);

        // 9. Persist the aggregate
        StockInitialization saved = initializationRepository.save(initialization);

        // 10. Post one stock movement per line — atomic: all in this transaction
        for (StockInitializationItem item : saved.getItems()) {
            stockLedger.post(item.getProductId(), saved.getLocationId(),
                    MovementType.INITIALIZATION, item.getBaseQuantity(),
                    ReferenceType.STOCK_INITIALIZATION, saved.getInitializationId(),
                    command.requestedBy(), null,
                    saved.getInitializationNumber() + " initial stock");
        }

        // 11. Record the audit entry
        List<InventoryAuditPort.Change> changes = saved.getItems().stream()
                .map(item -> new InventoryAuditPort.Change(
                        "product:" + item.getProductId(), null, String.valueOf(item.getBaseQuantity())))
                .toList();
        audit.record(new InventoryAuditPort.AuditRecord(
                command.requestedBy(), "STOCK.INITIALIZATION_COMPLETED", "STOCK_INITIALIZATION",
                saved.getInitializationId(), saved.getLocationId(),
                "Initial stock initialized: " + saved.getItems().size() + " products", changes));

        // 12. Build the result with enriched names
        List<StockInitializationItemResult> itemResults = buildItemResults(saved, productDetails);
        return StockInitializationResult.from(saved, location.locationName(), itemResults);
    }

    @Override
    @Transactional(readOnly = true)
    public StockInitializationResult getInitialization(UUID initializationId, UUID viewerUserId,
                                                       boolean restrictedToAssignedLocation) {
        StockInitialization init = initializationRepository.findById(initializationId)
                .orElseThrow(() -> new StockInitializationNotFoundException(initializationId));
        accessScope.checkCanAct(init.getLocationId(), viewerUserId, restrictedToAssignedLocation);
        return toResult(init);
    }

    @Override
    @Transactional(readOnly = true)
    public StockInitializationPageResult listInitializations(StockInitializationPageRequest request) {
        StockInitializationStatus status = request.status() == null || request.status().isBlank()
                ? null : StockInitializationStatus.from(request.status());
        UUID locationId = accessScope.effectiveLocation(request.locationId(), request.viewerUserId(),
                request.restrictedToAssignedLocation());
        StockInitializationFilter filter = new StockInitializationFilter(status, locationId);

        List<StockInitialization> initializations = initializationRepository.findAll(filter, request.page(), request.size());
        long total = initializationRepository.count(filter);

        // Batch-lookup location names
        Set<UUID> locationIds = initializations.stream()
                .map(StockInitialization::getLocationId).collect(Collectors.toSet());
        Map<UUID, InventoryLocation> locations = locationDirectory.findAllByIds(locationIds);

        // Batch-lookup product names (without unit detail — too expensive for a listing)
        Set<UUID> productIds = initializations.stream()
                .flatMap(i -> i.getItems().stream().map(StockInitializationItem::getProductId))
                .collect(Collectors.toSet());
        Map<UUID, InventoryProduct> products = productCatalog.findAllByIds(productIds);

        List<StockInitializationResult> content = initializations.stream()
                .map(init -> {
                    InventoryLocation loc = locations.get(init.getLocationId());
                    List<StockInitializationItemResult> itemResults = init.getItems().stream()
                            .map(item -> {
                                InventoryProduct product = products.get(item.getProductId());
                                return StockInitializationItemResult.from(item,
                                        product != null ? product.productName() : null, null, null);
                            })
                            .toList();
                    return StockInitializationResult.from(init,
                            loc != null ? loc.locationName() : null, itemResults);
                })
                .toList();

        return StockInitializationPageResult.of(content, request.page(), request.size(), total);
    }

    /** Enriches items using product details already fetched during initialization. */
    private List<StockInitializationItemResult> buildItemResults(StockInitialization init,
                                                                 Map<UUID, InventoryProductDetail> productDetails) {
        return init.getItems().stream()
                .map(item -> {
                    InventoryProductDetail pd = productDetails.get(item.getProductId());
                    String productName = pd != null ? pd.productName() : null;
                    InventoryProductUnit unit = pd != null
                            ? pd.units().stream().filter(u -> u.unitId().equals(item.getUnitId())).findFirst().orElse(null)
                            : null;
                    return StockInitializationItemResult.from(item, productName,
                            unit != null ? unit.unitCode() : null,
                            unit != null ? unit.unitName() : null);
                })
                .toList();
    }

    /** Enriches a single initialization with full product and unit detail (for get-by-id). */
    private StockInitializationResult toResult(StockInitialization init) {
        String locationName = locationDirectory.findLocation(init.getLocationId())
                .map(InventoryLocation::locationName).orElse(null);

        List<StockInitializationItemResult> itemResults = init.getItems().stream()
                .map(item -> {
                    InventoryProductDetail detail = productCatalog.findProductDetail(item.getProductId()).orElse(null);
                    String productName = detail != null ? detail.productName() : null;
                    InventoryProductUnit unit = detail != null
                            ? detail.units().stream().filter(u -> u.unitId().equals(item.getUnitId())).findFirst().orElse(null)
                            : null;
                    return StockInitializationItemResult.from(item, productName,
                            unit != null ? unit.unitCode() : null,
                            unit != null ? unit.unitName() : null);
                })
                .toList();

        return StockInitializationResult.from(init, locationName, itemResults);
    }
}
