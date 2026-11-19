package tsgp.yah_enterprise.sales.application.service;

import tsgp.yah_enterprise.sales.application.dto.*;
import tsgp.yah_enterprise.sales.domain.exception.*;
import tsgp.yah_enterprise.sales.domain.model.*;
import tsgp.yah_enterprise.sales.domain.port.*;
import tsgp.yah_enterprise.sales.domain.valueobject.Money;

import java.time.Instant;
import java.util.*;

/** Shared checks, pricing and mapping of the Sales services. */
final class OrderSupport {

    private final SalesCustomerDirectoryPort customers;
    private final SalesProductCatalogPort products;
    private final SalesPricingPort pricing;
    private final SalesLocationDirectoryPort locations;

    OrderSupport(SalesCustomerDirectoryPort customers, SalesProductCatalogPort products, SalesPricingPort pricing,
                 SalesLocationDirectoryPort locations) {
        this.customers = customers;
        this.products = products;
        this.pricing = pricing;
        this.locations = locations;
    }

    static void requireActor(OrderActor actor) {
        if (actor == null || actor.userId() == null) {
            throw new InvalidOrderException("The acting user is required");
        }
    }

    void requireAtLocation(OrderActor actor, UUID locationId) {
        requireActor(actor);
        if (!actor.anyLocation() && !locations.isUserAssigned(actor.userId(), locationId)) {
            throw new OrderLocationAccessException("You are not assigned to this location");
        }
    }

    void requireUsableLocation(UUID locationId) {
        boolean usable = locations.findLocation(locationId).map(SalesLocationDirectoryPort.SalesLocation::active)
                .orElse(false);
        if (!usable) {
            throw new OrderLocationNotUsableException("Location " + locationId + " does not exist or is inactive");
        }
    }

    void requireUsableCustomer(UUID customerId) {
        boolean usable = customers.findById(customerId).map(SalesCustomerDirectoryPort.SalesCustomer::active)
                .orElse(false);
        if (!usable) {
            throw new OrderCustomerNotUsableException("Customer " + customerId + " does not exist or is inactive");
        }
    }

    /** Users whose role works across every location may filter by any location; the others see only their own. */
    UUID resolveScope(OrderActor actor, UUID requested) {
        requireActor(actor);
        if (actor.anyLocation()) {
            return requested;
        }
        UUID own = locations.findLocationIdOfUser(actor.userId())
                .orElseThrow(() -> new OrderLocationAccessException("You are not assigned to a location"));
        if (requested != null && !requested.equals(own)) {
            throw new OrderLocationAccessException("You are not assigned to this location");
        }
        return own;
    }

    /** Checks each product and unit through the catalog and copies the conversion factor onto the line (F12). */
    List<OrderItem> buildLines(List<OrderLineCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new InvalidOrderException("An order needs at least one line");
        }
        Map<UUID, SalesProductCatalogPort.SalesProduct> catalog = new HashMap<>();
        List<OrderItem> lines = new ArrayList<>();
        for (OrderLineCommand c : commands) {
            if (c == null || c.productId() == null || c.unitId() == null) {
                throw new InvalidOrderException("Every line needs a product and a unit");
            }
            SalesProductCatalogPort.SalesProduct product = catalog.get(c.productId());
            if (product == null) {
                product = products.findProduct(c.productId()).orElseThrow(() -> new OrderProductNotSellableException(
                        "Product " + c.productId() + " does not exist"));
                catalog.put(c.productId(), product);
            }
            final SalesProductCatalogPort.SalesProduct found = product;
            if (!found.active()) {
                throw new OrderProductNotSellableException("Product " + found.productName() + " is inactive");
            }
            SalesProductCatalogPort.SalesProductUnit unit = found.units().stream()
                    .filter(u -> u.unitId().equals(c.unitId()) && u.active()).findFirst()
                    .orElseThrow(() -> new OrderProductNotSellableException("Unit " + c.unitId()
                            + " is not an active unit of " + found.productName()));
            lines.add(OrderItem.create(c.productId(), c.unitId(), unit.conversionFactor(), c.quantity()));
        }
        return lines;
    }

    /** The price for one line now, or null when pricing has none (no fallback to another unit). */
    PriceQuote quote(OrderItem item) {
        return pricing.applicablePrice(item.getProductId(), item.getUnitId(), item.getQuantity(), Instant.now())
                .map(p -> new PriceQuote(p.priceId(), Money.of(p.unitPrice()))).orElse(null);
    }

    String productName(UUID productId) {
        return products.findProduct(productId).map(SalesProductCatalogPort.SalesProduct::productName)
                .orElse(productId.toString());
    }

    static OrderItemResult toResult(OrderItem i) {
        return new OrderItemResult(i.getOrderItemId(), i.getProductId(), i.getUnitId(), i.getConversionFactor(),
                i.getQuantity(), i.baseQuantity(), i.getUnitPrice() == null ? null : i.getUnitPrice().amount(),
                i.getPriceId(), i.getDiscountAmount().amount(), i.getLineTotal().amount());
    }

    static OrderResult toResult(Order o) {
        return new OrderResult(o.getOrderId(), o.getOrderNumber().value(), o.getLocationId(), o.getCustomerId(),
                o.getOrderStatus().name(), o.getItems().stream().map(OrderSupport::toResult).toList(),
                o.getSubtotal().amount(), o.getDiscountTotal().amount(), o.getTotalAmount().amount(),
                o.getAmountPaid() == null ? null : o.getAmountPaid().amount(),
                o.getAmountDue() == null ? null : o.getAmountDue().amount(), o.getNote(), o.getCreatedBy(),
                o.getValidatedBy(), o.getCompletedBy(), o.getCancelledBy(), o.getRejectionReason(),
                o.getCancelReason(), o.getCreatedAt(), o.getSubmittedAt(), o.getValidatedAt(), o.getCompletedAt(),
                o.getUpdatedAt());
    }
}
