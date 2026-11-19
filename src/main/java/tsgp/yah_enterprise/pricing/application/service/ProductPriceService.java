package tsgp.yah_enterprise.pricing.application.service;

import tsgp.yah_enterprise.pricing.domain.port.PricingAuditPort;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tsgp.yah_enterprise.pricing.application.dto.ApplicablePriceResult;
import tsgp.yah_enterprise.pricing.application.dto.ChangeProductPriceCommand;
import tsgp.yah_enterprise.pricing.application.dto.CreateProductPriceCommand;
import tsgp.yah_enterprise.pricing.application.dto.PricePageRequest;
import tsgp.yah_enterprise.pricing.application.dto.PricePageResult;
import tsgp.yah_enterprise.pricing.application.dto.ProductPriceResult;
import tsgp.yah_enterprise.pricing.application.port.in.ChangeProductPriceUseCase;
import tsgp.yah_enterprise.pricing.application.port.in.CreateProductPriceUseCase;
import tsgp.yah_enterprise.pricing.application.port.in.DeactivateProductPriceUseCase;
import tsgp.yah_enterprise.pricing.application.port.in.GetApplicablePriceUseCase;
import tsgp.yah_enterprise.pricing.application.port.in.GetProductPriceUseCase;
import tsgp.yah_enterprise.pricing.application.port.in.ListPriceHistoryUseCase;
import tsgp.yah_enterprise.pricing.application.port.in.ListProductPricesUseCase;
import tsgp.yah_enterprise.pricing.domain.exception.InvalidPriceException;
import tsgp.yah_enterprise.pricing.domain.exception.InvalidQuantityRangeException;
import tsgp.yah_enterprise.pricing.domain.exception.PriceProductNotPriceableException;
import tsgp.yah_enterprise.pricing.domain.exception.ProductPriceNotFoundException;
import tsgp.yah_enterprise.pricing.domain.model.ProductPrice;
import tsgp.yah_enterprise.pricing.domain.port.PriceableProduct;
import tsgp.yah_enterprise.pricing.domain.port.PriceableUnit;
import tsgp.yah_enterprise.pricing.domain.port.ProductCatalogPort;
import tsgp.yah_enterprise.pricing.domain.repository.ProductPriceRepository;
import tsgp.yah_enterprise.pricing.domain.service.PriceOverlapPolicy;
import tsgp.yah_enterprise.pricing.domain.service.PriceResolver;
import tsgp.yah_enterprise.pricing.domain.valueobject.Money;
import tsgp.yah_enterprise.pricing.domain.valueobject.QuantityRange;

/**
 * Application service implementing all pricing use cases.
 * Who may call each use case is decided by the web/security layer.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductPriceService implements
        CreateProductPriceUseCase,
        ChangeProductPriceUseCase,
        DeactivateProductPriceUseCase,
        GetProductPriceUseCase,
        ListProductPricesUseCase,
        ListPriceHistoryUseCase,
        GetApplicablePriceUseCase {

    private final ProductPriceRepository priceRepository;
    private final PricingAuditPort audit;
    private final ProductCatalogPort productCatalogPort;

    // Stateless domain services: Lombok leaves initialised fields out of the constructor.
    private final PriceOverlapPolicy overlapPolicy = new PriceOverlapPolicy();
    private final PriceResolver priceResolver = new PriceResolver();

    @Override
    public ProductPriceResult createProductPrice(CreateProductPriceCommand command) {
        ensurePriceable(command.productId(), command.unitId());

        ProductPrice candidate = ProductPrice.create(command.productId(), command.unitId(),
                command.price(), command.minimumQuantity(), command.maximumQuantity(),
                command.effectiveFrom(), command.effectiveTo());

        // Friendly pre-check; the database exclusion constraint is the real guarantee under concurrency.
        overlapPolicy.ensureNoOverlap(candidate,
                priceRepository.findActiveByProductAndUnit(command.productId(), command.unitId()));

        ProductPrice created = priceRepository.save(candidate);
        audit.record(new PricingAuditPort.AuditRecord(null, "PRICE.CREATED", "PRICE", created.getPriceId(), null, null,
                List.of(new PricingAuditPort.Change("price", null, String.valueOf(created.getPrice().amount())), new PricingAuditPort.Change("productId", null, String.valueOf(created.getProductId())))));
        return ProductPriceResult.from(created);
    }

    @Override
    public ProductPriceResult changeProductPrice(UUID productId, UUID priceId, ChangeProductPriceCommand command) {
        if (command.price() == null && command.minimumQuantity() == null && command.maximumQuantity() == null) {
            throw new InvalidPriceException("Nothing to change: give a price and/or a quantity range");
        }
        if (command.minimumQuantity() == null && command.maximumQuantity() != null) {
            throw new InvalidQuantityRangeException("minimumQuantity is required when the quantity range changes");
        }

        ProductPrice current = findPrice(productId, priceId);
        ensurePriceable(productId, current.getUnitId());

        Money newAmount = command.price() == null ? current.getPrice() : Money.of(command.price());
        QuantityRange newRange = command.minimumQuantity() == null
                ? current.getQuantityRange()
                : QuantityRange.of(command.minimumQuantity(), command.maximumQuantity());

        if (newAmount.equals(current.getPrice()) && newRange.equals(current.getQuantityRange())) {
            return ProductPriceResult.from(current);          // same values: no new row, no noise in the history
        }

        // History is never rewritten: the old row is closed, a successor carries the new values.
        ProductPrice successor = current.supersede(newAmount, newRange, Instant.now());

        // The closed row no longer conflicts; the check runs on the successor only.
        overlapPolicy.ensureNoOverlap(successor,
                priceRepository.findActiveByProductAndUnit(productId, current.getUnitId()));

        priceRepository.save(current);                         // close the old row first, so the two never overlap
        ProductPrice changed = priceRepository.save(successor);
        audit.record(new PricingAuditPort.AuditRecord(null, "PRICE.CHANGED", "PRICE", changed.getPriceId(), null, null,
                List.of(new PricingAuditPort.Change("price", String.valueOf(current.getPrice().amount()), String.valueOf(changed.getPrice().amount())), new PricingAuditPort.Change("previousPriceId", null, String.valueOf(current.getPriceId())))));
        return ProductPriceResult.from(changed);
    }

    @Override
    public ProductPriceResult deactivateProductPrice(UUID productId, UUID priceId) {
        ProductPrice price = findPrice(productId, priceId);
        price.deactivate();
        ProductPrice saved = priceRepository.save(price);
        audit.record(new PricingAuditPort.AuditRecord(null, "PRICE.DEACTIVATED", "PRICE", priceId, null, null,
                List.of(new PricingAuditPort.Change("priceStatus", "ACTIVE", "INACTIVE"))));
        return ProductPriceResult.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPriceResult getProductPrice(UUID productId, UUID priceId) {
        return ProductPriceResult.from(findPrice(productId, priceId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductPriceResult> listProductPrices(UUID productId, UUID unitId) {
        Instant now = Instant.now();
        return priceRepository.findActiveByProduct(productId).stream()
                .filter(price -> unitId == null || price.getUnitId().equals(unitId))
                .filter(price -> price.isEffectiveAt(now))
                .map(ProductPriceResult::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PricePageResult listPriceHistory(UUID productId, UUID unitId, PricePageRequest request) {
        List<ProductPriceResult> content = priceRepository
                .findByProduct(productId, unitId, request.page(), request.size())
                .stream()
                .map(ProductPriceResult::from)
                .toList();

        long total = priceRepository.countByProduct(productId, unitId);
        return PricePageResult.of(content, request.page(), request.size(), total);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicablePriceResult getApplicablePrice(UUID productId, UUID unitId, int quantity, Instant at) {
        Instant moment = at == null ? Instant.now() : at;
        ProductPrice price = priceResolver.resolve(productId, unitId,
                priceRepository.findActiveByProductAndUnit(productId, unitId), quantity, moment);

        return new ApplicablePriceResult(price.getPriceId(), price.getPrice().amount(), quantity,
                price.getPrice().times(quantity).amount());
    }

    // ---- helpers ----

    private ProductPrice findPrice(UUID productId, UUID priceId) {
        // A price id that belongs to another product is reported as "not found", never leaked.
        return priceRepository.findById(priceId)
                .filter(price -> price.getProductId().equals(productId))
                .orElseThrow(() -> new ProductPriceNotFoundException(priceId));
    }

    /** A price may only be set for an ACTIVE product and an ACTIVE unit of that product. */
    private void ensurePriceable(UUID productId, UUID unitId) {
        PriceableProduct product = productCatalogPort.findProduct(productId)
                .orElseThrow(() -> new PriceProductNotPriceableException("Product not found: " + productId));
        if (!product.active()) {
            throw new PriceProductNotPriceableException("Product is inactive: " + productId);
        }
        PriceableUnit unit = product.findUnit(unitId)
                .orElseThrow(() -> new PriceProductNotPriceableException(
                        "Unit " + unitId + " is not a unit of product " + productId));
        if (!unit.active()) {
            throw new PriceProductNotPriceableException("Unit is inactive for this product: " + unitId);
        }
    }
}
