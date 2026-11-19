package tsgp.yah_enterprise.inventory.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import tsgp.yah_enterprise.inventory.domain.port.InventoryAuditPort;
import tsgp.yah_enterprise.inventory.application.dto.DecideStockAdjustmentCommand;
import tsgp.yah_enterprise.inventory.application.dto.RequestStockAdjustmentCommand;
import tsgp.yah_enterprise.inventory.application.dto.StockAdjustmentPageRequest;
import tsgp.yah_enterprise.inventory.application.dto.StockAdjustmentPageResult;
import tsgp.yah_enterprise.inventory.application.dto.StockAdjustmentResult;
import tsgp.yah_enterprise.inventory.application.port.in.ApproveStockAdjustmentUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.GetStockAdjustmentUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.ListStockAdjustmentsUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.RejectStockAdjustmentUseCase;
import tsgp.yah_enterprise.inventory.application.port.in.RequestStockAdjustmentUseCase;
import tsgp.yah_enterprise.inventory.domain.exception.StockAdjustmentNotFoundException;
import tsgp.yah_enterprise.inventory.domain.exception.StockLocationNotUsableException;
import tsgp.yah_enterprise.inventory.domain.exception.StockProductNotUsableException;
import tsgp.yah_enterprise.inventory.domain.model.AdjustmentDirection;
import tsgp.yah_enterprise.inventory.domain.model.AdjustmentReason;
import tsgp.yah_enterprise.inventory.domain.model.AdjustmentStatus;
import tsgp.yah_enterprise.inventory.domain.model.MovementType;
import tsgp.yah_enterprise.inventory.domain.model.ReferenceType;
import tsgp.yah_enterprise.inventory.domain.model.StockAdjustment;
import tsgp.yah_enterprise.inventory.domain.model.StockMovement;
import tsgp.yah_enterprise.inventory.domain.port.InventoryLocation;
import tsgp.yah_enterprise.inventory.domain.port.InventoryLocationDirectoryPort;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProduct;
import tsgp.yah_enterprise.inventory.domain.port.InventoryProductCatalogPort;
import tsgp.yah_enterprise.inventory.domain.repository.StockAdjustmentFilter;
import tsgp.yah_enterprise.inventory.domain.repository.StockAdjustmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Stock adjustments: requested by one user and decided by any user with STOCK_ADJUST_APPROVE, the requester
 * included. Approval is the only moment an adjustment moves stock, and it does so in the same transaction as
 * the status change.
 */
@Service
@Transactional
public class StockAdjustmentService implements
        RequestStockAdjustmentUseCase,
        ApproveStockAdjustmentUseCase,
        RejectStockAdjustmentUseCase,
        GetStockAdjustmentUseCase,
        ListStockAdjustmentsUseCase {

    private final StockAdjustmentRepository adjustmentRepository;
    private final InventoryAuditPort audit;
    private final StockLedger stockLedger;
    private final StockAccessScope accessScope;
    private final InventoryProductCatalogPort productCatalog;
    private final InventoryLocationDirectoryPort locationDirectory;

    /** Kept for callers that do not audit (the unit tests). The Spring constructor is the next one. */
    public StockAdjustmentService(StockAdjustmentRepository adjustmentRepository, StockLedger stockLedger,
                                  StockAccessScope accessScope, InventoryProductCatalogPort productCatalog,
                                  InventoryLocationDirectoryPort locationDirectory) {
        this(adjustmentRepository, stockLedger, accessScope, productCatalog, locationDirectory, InventoryAuditPort.NOOP);
    }

    @Autowired
    public StockAdjustmentService(StockAdjustmentRepository adjustmentRepository, StockLedger stockLedger,
                                  StockAccessScope accessScope, InventoryProductCatalogPort productCatalog,
                                  InventoryLocationDirectoryPort locationDirectory,
                                  InventoryAuditPort audit) {
        this.adjustmentRepository = adjustmentRepository;
        this.stockLedger = stockLedger;
        this.accessScope = accessScope;
        this.productCatalog = productCatalog;
        this.locationDirectory = locationDirectory;
        this.audit = audit;
    }

    @Override
    public StockAdjustmentResult requestAdjustment(RequestStockAdjustmentCommand command) {
        accessScope.checkCanAct(command.locationId(), command.requestedBy(), command.restrictedToAssignedLocation());

        AdjustmentDirection direction = AdjustmentDirection.from(command.direction());
        AdjustmentReason reason = AdjustmentReason.from(command.reasonCategory());

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

        StockAdjustment adjustment = StockAdjustment.request(adjustmentRepository.nextNumber(),
                command.productId(), command.locationId(), direction, reason, command.quantity(),
                command.note(), command.requestedBy());
        StockAdjustment saved = adjustmentRepository.save(adjustment);
        audit.record(new InventoryAuditPort.AuditRecord(command.requestedBy(), "STOCK.ADJUSTMENT_REQUESTED", "STOCK_ADJUSTMENT", saved.getAdjustmentId(), command.locationId(), command.note(),
                List.of(new InventoryAuditPort.Change("direction", null, String.valueOf(saved.getDirection())), new InventoryAuditPort.Change("quantity", null, String.valueOf(saved.getQuantity())), new InventoryAuditPort.Change("reason", null, String.valueOf(saved.getReasonCategory())))));
        return StockAdjustmentResult.from(saved, product.productName(), location.locationName());
    }

    @Override
    public StockAdjustmentResult approve(DecideStockAdjustmentCommand command) {
        StockAdjustment adjustment = findForUpdate(command.adjustmentId());
        accessScope.checkCanAct(adjustment.getLocationId(), command.decidedBy(),
                command.restrictedToAssignedLocation());

        // Refuse a second approval before the ledger is touched.
        adjustment.ensureCanBeDecidedBy(command.decidedBy());

        StockMovement movement = stockLedger.post(adjustment.getProductId(), adjustment.getLocationId(),
                MovementType.ADJUSTMENT, adjustment.signedQuantity(), ReferenceType.ADJUSTMENT,
                adjustment.getAdjustmentId(), command.decidedBy(), null,
                adjustment.getAdjustmentNumber() + " " + adjustment.getReasonCategory());

        long quantityBefore = movement.getBalanceAfter() - movement.getQuantityDelta();
        adjustment.approve(command.decidedBy(), quantityBefore, command.note());
        StockAdjustment saved = adjustmentRepository.save(adjustment);
        audit.record(new InventoryAuditPort.AuditRecord(command.decidedBy(), "STOCK.ADJUSTMENT_APPROVED", "STOCK_ADJUSTMENT", saved.getAdjustmentId(), saved.getLocationId(), command.note(),
                List.of(new InventoryAuditPort.Change("status", "PENDING", "APPROVED"), new InventoryAuditPort.Change("stockQuantity", String.valueOf(quantityBefore), String.valueOf(quantityBefore + saved.signedQuantity())))));
        return toResult(saved);
    }

    @Override
    public StockAdjustmentResult reject(DecideStockAdjustmentCommand command) {
        StockAdjustment adjustment = findForUpdate(command.adjustmentId());
        accessScope.checkCanAct(adjustment.getLocationId(), command.decidedBy(),
                command.restrictedToAssignedLocation());

        adjustment.reject(command.decidedBy(), command.note());
        StockAdjustment saved = adjustmentRepository.save(adjustment);
        audit.record(new InventoryAuditPort.AuditRecord(command.decidedBy(), "STOCK.ADJUSTMENT_REJECTED", "STOCK_ADJUSTMENT", saved.getAdjustmentId(), saved.getLocationId(), command.note(),
                List.of(new InventoryAuditPort.Change("status", "PENDING", "REJECTED"))));
        return toResult(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StockAdjustmentResult getAdjustment(UUID adjustmentId, UUID viewerUserId,
                                               boolean restrictedToAssignedLocation) {
        StockAdjustment adjustment = adjustmentRepository.findById(adjustmentId)
                .orElseThrow(() -> new StockAdjustmentNotFoundException(adjustmentId));
        accessScope.checkCanAct(adjustment.getLocationId(), viewerUserId, restrictedToAssignedLocation);
        return toResult(adjustment);
    }

    @Override
    @Transactional(readOnly = true)
    public StockAdjustmentPageResult listAdjustments(StockAdjustmentPageRequest request) {
        AdjustmentStatus status = request.status() == null || request.status().isBlank()
                ? null : AdjustmentStatus.from(request.status());
        UUID locationId = accessScope.effectiveLocation(request.locationId(), request.viewerUserId(),
                request.restrictedToAssignedLocation());
        StockAdjustmentFilter filter = new StockAdjustmentFilter(status, locationId, request.productId());

        List<StockAdjustment> adjustments = adjustmentRepository.findAll(filter, request.page(), request.size());
        long total = adjustmentRepository.count(filter);

        Set<UUID> productIds = adjustments.stream().map(StockAdjustment::getProductId).collect(Collectors.toSet());
        Set<UUID> locationIds = adjustments.stream().map(StockAdjustment::getLocationId).collect(Collectors.toSet());
        Map<UUID, InventoryProduct> products = productCatalog.findAllByIds(productIds);
        Map<UUID, InventoryLocation> locations = locationDirectory.findAllByIds(locationIds);

        List<StockAdjustmentResult> content = adjustments.stream().map(a -> {
            InventoryProduct product = products.get(a.getProductId());
            InventoryLocation location = locations.get(a.getLocationId());
            return StockAdjustmentResult.from(a,
                    product == null ? null : product.productName(),
                    location == null ? null : location.locationName());
        }).toList();
        return StockAdjustmentPageResult.of(content, request.page(), request.size(), total);
    }

    private StockAdjustment findForUpdate(UUID adjustmentId) {
        return adjustmentRepository.findByIdForUpdate(adjustmentId)
                .orElseThrow(() -> new StockAdjustmentNotFoundException(adjustmentId));
    }

    private StockAdjustmentResult toResult(StockAdjustment adjustment) {
        String productName = productCatalog.findProduct(adjustment.getProductId())
                .map(InventoryProduct::productName).orElse(null);
        String locationName = locationDirectory.findLocation(adjustment.getLocationId())
                .map(InventoryLocation::locationName).orElse(null);
        return StockAdjustmentResult.from(adjustment, productName, locationName);
    }
}