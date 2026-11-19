package tsgp.yah_enterprise.inventory.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import tsgp.yah_enterprise.inventory.domain.port.InventoryAuditPort;
import tsgp.yah_enterprise.inventory.application.dto.ProductStockResult;
import tsgp.yah_enterprise.inventory.application.dto.RecordStockMovementCommand;
import tsgp.yah_enterprise.inventory.application.dto.StockLevelPageRequest;
import tsgp.yah_enterprise.inventory.application.dto.StockLevelPageResult;
import tsgp.yah_enterprise.inventory.application.dto.StockLevelResult;
import tsgp.yah_enterprise.inventory.application.dto.StockMovementPageRequest;
import tsgp.yah_enterprise.inventory.application.dto.StockMovementPageResult;
import tsgp.yah_enterprise.inventory.application.dto.StockMovementResult;
import tsgp.yah_enterprise.inventory.application.dto.UpdateStockThresholdCommand;
import tsgp.yah_enterprise.inventory.application.port.in.GetProductStockUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.ListStockLevelsUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.ListStockMovementsUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.RecordStockMovementUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.UpdateStockThresholdUseCase;
import tsgp.yah_enterprise.inventory.domain.exception.InvalidStockMovementException;
import tsgp.yah_enterprise.inventory.domain.exception.StockLocationNotUsableException;
import tsgp.yah_enterprise.inventory.domain.exception.StockProductNotUsableException;
import tsgp.yah_enterprise.inventory.domain.model.MovementType;
import tsgp.yah_enterprise.inventory.domain.model.ReferenceType;
import tsgp.yah_enterprise.inventory.domain.model.StockLevel;
import tsgp.yah_enterprise.inventory.domain.model.StockMovement;
import tsgp.yah_enterprise.inventory.domain.port.InventoryLocation;
import tsgp.yah_enterprise.inventory.domain.port.InventoryLocationDirectoryPort;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProduct;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProductCatalogPort;
import tsgp.yah_enterprise.inventory.domain.repository.StockLevelFilter;
import tsgp.yah_enterprise.inventory.domain.repository.StockLevelRepository;
import tsgp.yah_enterprise.inventory.domain.repository.StockMovementFilter;
import tsgp.yah_enterprise.inventory.domain.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Stock levels, the ledger and thresholds. Orchestrates only; the rules live in the domain. */
@Service
@Transactional
public class StockService implements
        RecordStockMovementUseCase,
        GetProductStockUseCase,
        ListStockLevelsUseCase,
        ListStockMovementsUseCase,
        UpdateStockThresholdUseCase {

    private final StockLedger stockLedger;
    private final InventoryAuditPort audit;
    private final StockAccessScope accessScope;
    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryProductCatalogPort productCatalog;
    private final InventoryLocationDirectoryPort locationDirectory;

    /** Kept for callers that do not audit (the unit tests). The Spring constructor is the next one. */
    public StockService(StockLedger stockLedger, StockAccessScope accessScope,
                        StockLevelRepository stockLevelRepository, StockMovementRepository stockMovementRepository,
                        InventoryProductCatalogPort productCatalog, InventoryLocationDirectoryPort locationDirectory) {
        this(stockLedger, accessScope, stockLevelRepository, stockMovementRepository, productCatalog, locationDirectory, InventoryAuditPort.NOOP);
    }

    @Autowired
    public StockService(StockLedger stockLedger, StockAccessScope accessScope,
                        StockLevelRepository stockLevelRepository, StockMovementRepository stockMovementRepository,
                        InventoryProductCatalogPort productCatalog, InventoryLocationDirectoryPort locationDirectory,
                        InventoryAuditPort audit) {
        this.stockLedger = stockLedger;
        this.accessScope = accessScope;
        this.stockLevelRepository = stockLevelRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productCatalog = productCatalog;
        this.locationDirectory = locationDirectory;
        this.audit = audit;
    }

    @Override
    public StockMovementResult recordMovement(RecordStockMovementCommand command) {
        MovementType type = MovementType.from(command.movementType());
        if (type == MovementType.ADJUSTMENT) {
            throw new InvalidStockMovementException("Adjustments are recorded through the stock adjustment use cases");
        }
        ReferenceType referenceType = ReferenceType.from(command.referenceType());
        if (referenceType != type.requiredReferenceType()) {
            throw new InvalidStockMovementException(
                    type + " must reference a " + type.requiredReferenceType());
        }
        if (command.quantity() < 1) {
            throw new InvalidStockMovementException("Quantity must be at least 1 base unit");
        }
        if (command.productId() == null || command.locationId() == null
                || command.referenceId() == null || command.performedBy() == null) {
            throw new InvalidStockMovementException(
                    "productId, locationId, referenceId and performedBy are required");
        }

        InventoryProduct product = productCatalog.findProduct(command.productId())
                .orElseThrow(() -> new StockProductNotUsableException(command.productId(), "it does not exist"));
        InventoryLocation location = locationDirectory.findLocation(command.locationId())
                .orElseThrow(() -> new StockLocationNotUsableException(command.locationId(), "it does not exist"));

        long signedDelta = type.direction() == MovementType.Direction.OUT ? -command.quantity() : command.quantity();
        StockMovement movement = stockLedger.post(command.productId(), command.locationId(), type, signedDelta,
                referenceType, command.referenceId(), command.performedBy(), command.occurredAt(), command.note());
        return StockMovementResult.from(movement, product.productName(), location.locationName());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductStockResult getProductStock(UUID productId, UUID locationId) {
        return stockLevelRepository.findByProductAndLocation(productId, locationId)
                .map(ProductStockResult::from)
                .orElseGet(() -> ProductStockResult.empty(productId, locationId));
    }

    @Override
    @Transactional(readOnly = true)
    public StockLevelPageResult listStockLevels(StockLevelPageRequest request) {
        UUID locationId = accessScope.effectiveLocation(request.locationId(), request.viewerUserId(),
                request.restrictedToAssignedLocation());
        StockLevelFilter filter = new StockLevelFilter(locationId, request.productId(), request.lowStockOnly());

        List<StockLevel> levels = stockLevelRepository.findAll(filter, request.page(), request.size());
        long total = stockLevelRepository.count(filter);

        Map<UUID, InventoryProduct> products = productCatalog.findAllByIds(
                levels.stream().map(StockLevel::getProductId).collect(Collectors.toSet()));
        Map<UUID, InventoryLocation> locations = locationDirectory.findAllByIds(
                levels.stream().map(StockLevel::getLocationId).collect(Collectors.toSet()));

        List<StockLevelResult> content = levels.stream().map(level -> {
            InventoryProduct product = products.get(level.getProductId());
            InventoryLocation location = locations.get(level.getLocationId());
            return StockLevelResult.from(level,
                    product == null ? null : product.productCode(),
                    product == null ? null : product.productName(),
                    location == null ? null : location.locationName());
        }).toList();
        return StockLevelPageResult.of(content, request.page(), request.size(), total);
    }

    @Override
    @Transactional(readOnly = true)
    public StockMovementPageResult listStockMovements(StockMovementPageRequest request) {
        MovementType type = request.movementType() == null || request.movementType().isBlank()
                ? null : MovementType.from(request.movementType());
        UUID locationId = accessScope.effectiveLocation(request.locationId(), request.viewerUserId(),
                request.restrictedToAssignedLocation());
        StockMovementFilter filter = new StockMovementFilter(request.productId(), locationId, type,
                request.from(), request.to());

        List<StockMovement> movements = stockMovementRepository.findAll(filter, request.page(), request.size());
        long total = stockMovementRepository.count(filter);

        Set<UUID> productIds = movements.stream().map(StockMovement::getProductId).collect(Collectors.toSet());
        Set<UUID> locationIds = movements.stream().map(StockMovement::getLocationId).collect(Collectors.toSet());
        Map<UUID, InventoryProduct> products = productCatalog.findAllByIds(productIds);
        Map<UUID, InventoryLocation> locations = locationDirectory.findAllByIds(locationIds);

        List<StockMovementResult> content = movements.stream().map(m -> {
            InventoryProduct product = products.get(m.getProductId());
            InventoryLocation location = locations.get(m.getLocationId());
            return StockMovementResult.from(m,
                    product == null ? null : product.productName(),
                    location == null ? null : location.locationName());
        }).toList();
        return StockMovementPageResult.of(content, request.page(), request.size(), total);
    }

    @Override
    public ProductStockResult updateThreshold(UpdateStockThresholdCommand command) {
        accessScope.checkCanAct(command.locationId(), command.requestedBy(), command.restrictedToAssignedLocation());

        InventoryProduct product = productCatalog.findProduct(command.productId())
                .orElseThrow(() -> new StockProductNotUsableException(command.productId(), "it does not exist"));
        if (!product.active()) {
            throw new StockProductNotUsableException(command.productId(), "it is inactive");
        }
        InventoryLocation location = locationDirectory.findLocation(command.locationId())
                .orElseThrow(() -> new StockLocationNotUsableException(command.locationId(), "it does not exist"));
        if (!location.active()) {
            throw new StockLocationNotUsableException(command.locationId(), "it is inactive");
        }

        StockLevel level = stockLevelRepository.lockOrCreate(command.productId(), command.locationId());
        Long previousThreshold = level.getLowStockThreshold();
        level.changeThreshold(command.lowStockThreshold());
        StockLevel savedLevel = stockLevelRepository.save(level);
        audit.record(new InventoryAuditPort.AuditRecord(command.requestedBy(), "STOCK.THRESHOLD_CHANGED", "STOCK_LEVEL", command.productId(), command.locationId(), null,
                List.of(new InventoryAuditPort.Change("lowStockThreshold", String.valueOf(previousThreshold), String.valueOf(savedLevel.getLowStockThreshold())))));
        return ProductStockResult.from(savedLevel);
    }
}
