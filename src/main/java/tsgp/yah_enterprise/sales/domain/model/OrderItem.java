package tsgp.yah_enterprise.sales.domain.model;

import tsgp.yah_enterprise.sales.domain.exception.InvalidDiscountException;
import tsgp.yah_enterprise.sales.domain.exception.InvalidOrderException;
import tsgp.yah_enterprise.sales.domain.exception.InvalidOrderQuantityException;
import tsgp.yah_enterprise.sales.domain.valueobject.Money;

import java.util.Objects;
import java.util.UUID;

/** One line of an order. The price is null while the line cannot be priced (no fallback to another unit). */
public class OrderItem {

    private final UUID orderItemId;
    private final UUID productId;
    private final UUID unitId;
    private final int conversionFactor;
    private final int quantity;
    private Money unitPrice;
    private UUID priceId;
    private Money discountAmount;
    private Money lineTotal;

    private OrderItem(UUID orderItemId, UUID productId, UUID unitId, int conversionFactor, int quantity,
                      Money unitPrice, UUID priceId, Money discountAmount) {
        this.orderItemId = Objects.requireNonNull(orderItemId);
        if (productId == null || unitId == null) {
            throw new InvalidOrderException("Product and unit are required on every line");
        }
        if (quantity < 1) {
            throw new InvalidOrderQuantityException("Quantity must be at least 1");
        }
        if (conversionFactor < 1) {
            throw new InvalidOrderException("Conversion factor must be at least 1");
        }
        this.productId = productId;
        this.unitId = unitId;
        this.conversionFactor = conversionFactor;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.priceId = priceId;
        this.discountAmount = discountAmount == null ? Money.ZERO : discountAmount;
        recalculate();
    }

    public static OrderItem create(UUID productId, UUID unitId, int conversionFactor, int quantity) {
        return new OrderItem(UUID.randomUUID(), productId, unitId, conversionFactor, quantity, null, null, null);
    }

    public static OrderItem reconstitute(UUID orderItemId, UUID productId, UUID unitId, int conversionFactor,
                                         int quantity, Money unitPrice, UUID priceId, Money discountAmount) {
        return new OrderItem(orderItemId, productId, unitId, conversionFactor, quantity, unitPrice, priceId,
                discountAmount);
    }

    /** Same line, same row id (a replaced line for the same product and unit keeps its row). */
    OrderItem withId(UUID id) {
        return new OrderItem(id, productId, unitId, conversionFactor, quantity, unitPrice, priceId, discountAmount);
    }

    /** A null quote leaves the line unpriced and clears any discount. */
    void price(PriceQuote quote) {
        if (quote == null) {
            this.unitPrice = null;
            this.priceId = null;
            this.discountAmount = Money.ZERO;
        } else {
            this.unitPrice = quote.unitPrice();
            this.priceId = quote.priceId();
            if (discountAmount.isGreaterThan(gross())) {
                this.discountAmount = Money.ZERO;
            }
        }
        recalculate();
    }

    void applyDiscount(Money discount) {
        if (unitPrice == null) {
            throw new InvalidDiscountException("A line without a price cannot have a discount");
        }
        if (discount.isGreaterThan(gross())) {
            throw new InvalidDiscountException("The discount " + discount + " is more than the line amount "
                    + gross());
        }
        this.discountAmount = discount;
        recalculate();
    }

    /** quantity x unit price, before the discount. */
    public Money gross() {
        return unitPrice == null ? Money.ZERO : unitPrice.times(quantity);
    }

    public boolean isPriced() {
        return unitPrice != null;
    }

    public long baseQuantity() {
        return (long) quantity * conversionFactor;
    }

    private void recalculate() {
        this.lineTotal = gross().minus(discountAmount);
    }

    public boolean sameLine(UUID otherProductId, UUID otherUnitId) {
        return productId.equals(otherProductId) && unitId.equals(otherUnitId);
    }

    public UUID getOrderItemId() { return orderItemId; }
    public UUID getProductId() { return productId; }
    public UUID getUnitId() { return unitId; }
    public int getConversionFactor() { return conversionFactor; }
    public int getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }
    public UUID getPriceId() { return priceId; }
    public Money getDiscountAmount() { return discountAmount; }
    public Money getLineTotal() { return lineTotal; }
}
