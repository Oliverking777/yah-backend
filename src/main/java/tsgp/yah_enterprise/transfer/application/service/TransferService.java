package tsgp.yah_enterprise.transfer.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.transfer.application.dto.*;
import tsgp.yah_enterprise.transfer.application.port.in.*;
import tsgp.yah_enterprise.transfer.domain.exception.*;
import tsgp.yah_enterprise.transfer.domain.model.*;
import tsgp.yah_enterprise.transfer.domain.port.*;
import tsgp.yah_enterprise.transfer.domain.repository.PagedTransfers;
import tsgp.yah_enterprise.transfer.domain.repository.TransferRepository;
import tsgp.yah_enterprise.transfer.domain.valueobject.TransferNumber;

import java.time.Instant;
import java.util.*;

/**
 * The use cases of the Transfer module. Stock changes at exactly three moments of a transfer:
 * <ul>
 *   <li>send (create): TRANSFER_OUT at the source, full quantity;</li>
 *   <li>receipt: TRANSFER_IN at the destination, good quantity only;</li>
 *   <li>cancel: TRANSFER_IN back at the source, full quantity.</li>
 * </ul>
 * Each is written through Inventory in the same transaction as the status change, so a failure anywhere rolls
 * everything back.
 */
@Service
@Transactional
public class TransferService implements
        CreateTransferUseCase, GetTransferUseCase, ListTransfersUseCase, ListInTransitUseCase,
        ReceiveTransferUseCase, CancelTransferUseCase, GetTransferHistoryUseCase {

    private final TransferRepository repository;
    private final TransferAuditPort audit;
    private final TransferNumberSequence numberSequence;
    private final TransferProductCatalogPort productCatalog;
    private final TransferLocationDirectoryPort locationDirectory;
    private final TransferStockPort stock;

    /** Kept for callers that do not audit (the unit tests). The Spring constructor is the next one. */
    public TransferService(TransferRepository repository, TransferNumberSequence numberSequence,
                           TransferProductCatalogPort productCatalog,
                           TransferLocationDirectoryPort locationDirectory, TransferStockPort stock) {
        this(repository, numberSequence, productCatalog, locationDirectory, stock, TransferAuditPort.NOOP);
    }

    @Autowired
    public TransferService(TransferRepository repository, TransferNumberSequence numberSequence,
                           TransferProductCatalogPort productCatalog,
                           TransferLocationDirectoryPort locationDirectory, TransferStockPort stock,
                           TransferAuditPort audit) {
        this.repository = repository;
        this.numberSequence = numberSequence;
        this.productCatalog = productCatalog;
        this.locationDirectory = locationDirectory;
        this.stock = stock;
        this.audit = audit;
    }

    // ------------------------------------------------------------------ send, receive, cancel

    @Override
    public TransferResult createTransfer(CreateTransferCommand command) {
        if (command == null || command.actor() == null || command.actor().userId() == null) {
            throw new InvalidTransferException("The user creating the transfer is required");
        }
        TransferActor actor = command.actor();
        // a retry from an offline device: return what was stored, never take the stock out twice
        if (command.transferId() != null) {
            Optional<Transfer> existing = repository.findById(command.transferId());
            if (existing.isPresent()) {
                if (!actor.userId().equals(existing.get().getCreatedBy())) {
                    throw new TransferIdAlreadyUsedException(command.transferId());
                }
                return toResult(existing.get());
            }
        }
        if (command.sourceLocationId() == null || command.destinationLocationId() == null) {
            throw new InvalidTransferException("Source and destination location are required");
        }
        if (command.sourceLocationId().equals(command.destinationLocationId())) {
            throw new SameLocationTransferException();
        }
        requireAtLocation(actor, command.sourceLocationId());
        requireUsableLocation(command.sourceLocationId());
        requireUsableLocation(command.destinationLocationId());
        List<TransferItem> lines = buildLines(command.items());
        Transfer transfer = Transfer.submit(command.transferId(), TransferNumber.fromSequence(numberSequence.next()),
                command.sourceLocationId(), command.destinationLocationId(), command.note(), lines, actor.userId());

        moveAll(transfer, transfer.getSourceLocationId(), TransferStockPort.Direction.OUT,
                TransferItem::getQuantity, actor.userId(), transfer.getCreatedAt());
        Transfer saved = repository.save(transfer);
        audit.record(new TransferAuditPort.AuditRecord(actor.userId(), "TRANSFER.SUBMITTED", "TRANSFER",
                saved.getTransferId(), saved.getSourceLocationId(), null,
                List.of(new TransferAuditPort.Change("transferStatus", null, "SUBMITTED"))));
        return toResult(saved);
    }

    @Override
    public TransferResult receiveTransfer(ReceiveTransferCommand command) {
        Transfer transfer = findForUpdate(command.transferId());
        TransferActor actor = command.actor();
        requireAtLocation(actor, transfer.getDestinationLocationId());
        requireUsableLocation(transfer.getDestinationLocationId());

        List<TransferReceiptLine> lines = new ArrayList<>();
        for (ReceiveLineCommand line : command.items() == null ? List.<ReceiveLineCommand>of() : command.items()) {
            if (line == null) {
                throw new InvalidTransferException("A receipt line cannot be empty");
            }
            lines.add(new TransferReceiptLine(line.productId(), line.receivedQuantity(), line.damagedQuantity(),
                    line.discrepancyReason()));
        }
        transfer.receive(actor.userId(), lines);

        moveAll(transfer, transfer.getDestinationLocationId(), TransferStockPort.Direction.IN,
                TransferItem::getReceivedQuantity, actor.userId(), transfer.getReceivedAt());
        Transfer saved = repository.save(transfer);
        List<TransferAuditPort.Change> changes = new ArrayList<>();
        changes.add(new TransferAuditPort.Change("transferStatus", "SUBMITTED", "RECEIVED"));
        if (saved.hasDiscrepancy()) {
            changes.add(new TransferAuditPort.Change("hasDiscrepancy", "false", "true"));
        }
        audit.record(new TransferAuditPort.AuditRecord(actor.userId(), "TRANSFER.RECEIVED", "TRANSFER",
                saved.getTransferId(), saved.getDestinationLocationId(), null, changes));
        return toResult(saved);
    }

    @Override
    public TransferResult cancelTransfer(CancelTransferCommand command) {
        Transfer transfer = findForUpdate(command.transferId());
        TransferActor actor = command.actor();
        requireAtEitherLocation(actor, transfer);
        transfer.cancel(actor.userId(), command.reason());

        // the goods go back to the source in full
        moveAll(transfer, transfer.getSourceLocationId(), TransferStockPort.Direction.IN,
                TransferItem::getQuantity, actor.userId(), transfer.getCancelledAt());
        Transfer saved = repository.save(transfer);
        audit.record(new TransferAuditPort.AuditRecord(actor.userId(), "TRANSFER.CANCELLED", "TRANSFER",
                saved.getTransferId(), saved.getSourceLocationId(), saved.getCancelReason(),
                List.of(new TransferAuditPort.Change("transferStatus", "SUBMITTED", "CANCELLED"))));
        return toResult(saved);
    }

    // ------------------------------------------------------------------ reads

    @Override
    @Transactional(readOnly = true)
    public TransferResult getTransfer(UUID transferId, TransferActor actor) {
        return toResult(findReadable(transferId, actor));
    }

    @Override
    @Transactional(readOnly = true)
    public TransferPageResult listTransfers(TransferPageRequest request) {
        UUID scope = resolveScope(request.actor(), request.locationId());
        PagedTransfers found = repository.findAll(request.page(), request.size(), scope, request.status(),
                request.hasDiscrepancy(), request.productId(), request.from(), request.to());
        Map<UUID, TransferProduct> products = productsOf(found.items());
        Map<UUID, TransferLocation> locations = locationsOf(found.items());
        List<TransferResult> content = found.items().stream()
                .map(t -> TransferResult.of(t, products, locations)).toList();
        return TransferPageResult.of(content, request.page(), request.size(), found.total());
    }

    @Override
    @Transactional(readOnly = true)
    public InTransitResult listInTransit(UUID locationId, TransferActor actor) {
        UUID scope = resolveScope(actor, locationId);
        List<Transfer> travelling = repository.findInTransit(scope);
        // one line per destination and product: the base quantity still on its way
        Map<String, long[]> totals = new LinkedHashMap<>();
        Map<String, UUID[]> keys = new LinkedHashMap<>();
        for (Transfer t : travelling) {
            for (TransferItem item : t.getItems()) {
                String key = t.getDestinationLocationId() + "/" + item.getProductId();
                totals.computeIfAbsent(key, k -> new long[1])[0] += item.sentBaseQuantity();
                keys.putIfAbsent(key, new UUID[]{t.getDestinationLocationId(), item.getProductId()});
            }
        }
        Map<UUID, TransferProduct> products = productsOf(travelling);
        Map<UUID, TransferLocation> locations = locationsOf(travelling);
        List<InTransitLine> lines = new ArrayList<>();
        totals.forEach((key, total) -> {
            UUID destinationId = keys.get(key)[0];
            UUID productId = keys.get(key)[1];
            TransferLocation destination = locations.get(destinationId);
            TransferProduct product = products.get(productId);
            lines.add(new InTransitLine(destinationId, destination == null ? null : destination.locationName(),
                    productId, product == null ? null : product.productName(), total[0]));
        });
        return new InTransitResult(lines);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferEventResult> getTransferHistory(UUID transferId, TransferActor actor) {
        findReadable(transferId, actor);
        return repository.findEvents(transferId).stream().map(TransferEventResult::from).toList();
    }

    // ------------------------------------------------------------------ helpers

    /** Which quantity of a line a stock movement uses (in the line's unit). */
    private interface LineQuantity {
        Integer of(TransferItem item);
    }

    /**
     * One movement per line with a quantity above zero, lowest productId first, so two transfers touching the
     * same products lock the stock balances in the same order.
     */
    private void moveAll(Transfer transfer, UUID locationId, TransferStockPort.Direction direction,
                         LineQuantity quantity, UUID userId, Instant occurredAt) {
        for (TransferItem item : sortedByProduct(transfer)) {
            Integer q = quantity.of(item);
            if (q != null && q > 0) {
                stock.move(item.getProductId(), locationId, direction, item.baseQuantity(q),
                        transfer.getTransferId(), userId, occurredAt);
            }
        }
    }

    private Transfer findForUpdate(UUID transferId) {
        return repository.findByIdForUpdate(transferId).orElseThrow(() -> new TransferNotFoundException(transferId));
    }

    private Transfer findReadable(UUID transferId, TransferActor actor) {
        Transfer transfer = repository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(transferId));
        if (!actor.anyLocation()
                && !locationDirectory.isUserAssigned(actor.userId(), transfer.getSourceLocationId())
                && !locationDirectory.isUserAssigned(actor.userId(), transfer.getDestinationLocationId())) {
            throw new TransferLocationAccessException("You can only see transfers of your own location");
        }
        return transfer;
    }

    /** Users who work across every location may ask for any location (or none). Others see their own only. */
    private UUID resolveScope(TransferActor actor, UUID requestedLocationId) {
        if (actor.anyLocation()) {
            return requestedLocationId;
        }
        UUID own = locationDirectory.findLocationIdOfUser(actor.userId()).orElse(null);
        if (own == null) {
            throw new TransferLocationAccessException("You are not assigned to a location");
        }
        if (requestedLocationId != null && !requestedLocationId.equals(own)) {
            throw new TransferLocationAccessException("You can only see the transfers of your own location");
        }
        return own;
    }

    private void requireAtLocation(TransferActor actor, UUID locationId) {
        if (actor == null || actor.userId() == null) {
            throw new InvalidTransferException("The acting user is required");
        }
        if (!actor.anyLocation() && !locationDirectory.isUserAssigned(actor.userId(), locationId)) {
            throw new TransferLocationAccessException("You are not assigned to this location");
        }
    }

    /** The source or the destination: either side may call a transfer off. */
    private void requireAtEitherLocation(TransferActor actor, Transfer transfer) {
        if (actor == null || actor.userId() == null) {
            throw new InvalidTransferException("The acting user is required");
        }
        if (!actor.anyLocation()
                && !locationDirectory.isUserAssigned(actor.userId(), transfer.getSourceLocationId())
                && !locationDirectory.isUserAssigned(actor.userId(), transfer.getDestinationLocationId())) {
            throw new TransferLocationAccessException("You are not assigned to the source or the destination");
        }
    }

    /** Messages name the location; ids never reach the user. */
    private void requireUsableLocation(UUID locationId) {
        TransferLocation location = locationDirectory.findLocation(locationId)
                .orElseThrow(() -> new TransferLocationNotUsableException("This location no longer exists"));
        if (!location.active()) {
            throw new TransferLocationNotUsableException(location.locationName() + " is inactive");
        }
    }

    /**
     * Checks each product and unit through the catalog and copies the conversion factor onto the line (F12).
     * Messages name the product; ids never reach the user.
     */
    private List<TransferItem> buildLines(List<TransferLineCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new InvalidTransferException("A transfer needs at least one line");
        }
        List<TransferItem> lines = new ArrayList<>();
        for (TransferLineCommand c : commands) {
            if (c == null || c.productId() == null || c.unitId() == null) {
                throw new InvalidTransferException("Every line needs a product and a unit");
            }
            TransferProduct product = productCatalog.findProduct(c.productId())
                    .orElseThrow(() -> new TransferProductNotTransferableException(
                            "A product on this transfer no longer exists"));
            if (!product.active()) {
                throw new TransferProductNotTransferableException(product.productName() + " is inactive");
            }
            TransferProductUnit unit = product.unit(c.unitId())
                    .filter(TransferProductUnit::active)
                    .orElseThrow(() -> new TransferProductNotTransferableException(
                            "The chosen unit is not an active unit of " + product.productName()));
            lines.add(TransferItem.newLine(c.productId(), c.unitId(), unit.conversionFactor(), c.quantity()));
        }
        return lines;
    }

    private static List<TransferItem> sortedByProduct(Transfer transfer) {
        return transfer.getItems().stream()
                .sorted(Comparator.comparing(TransferItem::getProductId))
                .toList();
    }

    private TransferResult toResult(Transfer transfer) {
        return TransferResult.of(transfer, productsOf(List.of(transfer)), locationsOf(List.of(transfer)));
    }

    private Map<UUID, TransferProduct> productsOf(List<Transfer> transfers) {
        Set<UUID> ids = new HashSet<>();
        transfers.forEach(t -> t.getItems().forEach(i -> ids.add(i.getProductId())));
        return ids.isEmpty() ? Map.of() : productCatalog.findProducts(ids);
    }

    private Map<UUID, TransferLocation> locationsOf(List<Transfer> transfers) {
        Set<UUID> ids = new HashSet<>();
        transfers.forEach(t -> {
            ids.add(t.getSourceLocationId());
            ids.add(t.getDestinationLocationId());
        });
        return ids.isEmpty() ? Map.of() : locationDirectory.findLocations(ids);
    }
}