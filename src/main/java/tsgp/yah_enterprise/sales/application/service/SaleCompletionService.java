package tsgp.yah_enterprise.sales.application.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import tsgp.yah_enterprise.sales.domain.port.SalesAuditPort;
import tsgp.yah_enterprise.sales.application.dto.*;
import tsgp.yah_enterprise.sales.domain.exception.*;
import tsgp.yah_enterprise.sales.domain.model.*;
import tsgp.yah_enterprise.sales.domain.port.*;
import tsgp.yah_enterprise.sales.domain.valueobject.Money;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.sales.application.port.in.CompleteSaleUseCase;
import tsgp.yah_enterprise.sales.domain.repository.OrderRepository;
import tsgp.yah_enterprise.sales.domain.repository.ReceiptRepository;
import tsgp.yah_enterprise.sales.domain.valueobject.ReceiptNumber;

import java.time.Instant;
import java.util.*;

/**
 * The one place stock and money meet. In ONE transaction: a SALE movement per product (in productId order),
 * the settlement through Payment, the receipt, and the status COMPLETED. Any failure rolls everything back and the
 * order stays PENDING.
 */
@Service
@Transactional
public class SaleCompletionService implements CompleteSaleUseCase {

    private final OrderRepository orders;
    private final SalesAuditPort audit;
    private final ReceiptRepository receipts;
    private final ReceiptNumberSequence receiptSequence;
    private final SalesStockPort stock;
    private final SalesSettlementPort settlement;
    private final SalesCustomerDirectoryPort customers;
    private final SalesLocationDirectoryPort locations;
    private final OrderSupport support;

    /** Kept for callers that do not audit (the unit tests). The Spring constructor is the next one. */
    public SaleCompletionService(OrderRepository orders, ReceiptRepository receipts,
                                 ReceiptNumberSequence receiptSequence, SalesStockPort stock,
                                 SalesSettlementPort settlement, SalesCustomerDirectoryPort customers,
                                 SalesLocationDirectoryPort locations, SalesProductCatalogPort products,
                                 SalesPricingPort pricing) {
        this(orders, receipts, receiptSequence, stock, settlement, customers, locations, products, pricing, SalesAuditPort.NOOP);
    }

    @Autowired
    public SaleCompletionService(OrderRepository orders, ReceiptRepository receipts,
                                 ReceiptNumberSequence receiptSequence, SalesStockPort stock,
                                 SalesSettlementPort settlement, SalesCustomerDirectoryPort customers,
                                 SalesLocationDirectoryPort locations, SalesProductCatalogPort products,
                                 SalesPricingPort pricing,
                                 SalesAuditPort audit) {
        this.orders = orders;
        this.receipts = receipts;
        this.receiptSequence = receiptSequence;
        this.stock = stock;
        this.settlement = settlement;
        this.customers = customers;
        this.locations = locations;
        this.support = new OrderSupport(customers, products, pricing, locations);
        this.audit = audit;
    }

    @Override
    public SaleResult completeSale(CompleteSaleCommand c) {
        OrderActor actor = c.actor();
        OrderSupport.requireActor(actor);
        Order order = orders.findByIdForUpdate(c.orderId()).orElseThrow(() ->
                new OrderNotFoundException("Order " + c.orderId() + " not found"));
        if (!actor.anyLocation() && !locations.isUserAssigned(actor.userId(), order.getLocationId())) {
            throw new OrderLocationAccessException("You are not assigned to this location");
        }
        order.requireCompletable();
        SalesLocationDirectoryPort.SalesLocation location = locations.findLocation(order.getLocationId())
                .filter(SalesLocationDirectoryPort.SalesLocation::active).orElseThrow(() ->
                        new OrderLocationNotUsableException("Location " + order.getLocationId()
                                + " does not exist or is inactive"));
        SalesCustomerDirectoryPort.SalesCustomer customer = customers.findById(order.getCustomerId())
                .filter(SalesCustomerDirectoryPort.SalesCustomer::active).orElseThrow(() ->
                        new OrderCustomerNotUsableException("Customer " + order.getCustomerId()
                                + " does not exist or is inactive"));
        // the manager completes a PENDING order: price it again with today's price, apply the discounts, freeze
        order.priceLines(support::quote);
        List<Discount> discounts = new ArrayList<>();
        for (DiscountCommand d : c.discounts() == null ? List.<DiscountCommand>of() : c.discounts()) {
            if (d == null) {
                throw new InvalidOrderException("A discount cannot be empty");
            }
            discounts.add(new Discount(d.productId(), d.unitId(), Money.of(d.discountAmount())));
        }
        order.applyDiscounts(discounts);
        Instant now = Instant.now();

        // 1. stock out, one SALE movement per product, always in productId order (no deadlock between sales)
        for (Map.Entry<UUID, Long> need : order.baseQuantityByProduct().entrySet()) {
            stock.deduct(need.getKey(), order.getLocationId(), need.getValue(), order.getOrderId(),
                    actor.userId(), now);
        }

        // 2. money
        List<SalesSettlementPort.PaymentLine> lines = new ArrayList<>();
        for (PaymentLineCommand p : c.payments() == null ? List.<PaymentLineCommand>of() : c.payments()) {
            if (p == null || p.paymentMethod() == null) {
                throw new InvalidOrderException("Every payment needs a method and an amount");
            }
            lines.add(new SalesSettlementPort.PaymentLine(p.paymentMethod(), p.amount(), p.reference()));
        }
        SalesSettlementPort.SaleSettlement settled = settlement.settle(new SalesSettlementPort.SettlementRequest(
                order.getOrderId(), order.getCustomerId(), order.getLocationId(), order.getTotalAmount().amount(),
                lines, c.creditApprovalId(), actor.userId(), now));

        // 3. the order is now a sale, and gets its receipt
        order.complete(actor.userId(), Money.of(settled.amountPaid()), Money.of(settled.amountDue()));
        Order saved = orders.save(order);
        Receipt receipt = receipts.save(Receipt.issue(ReceiptNumber.fromSequence(receiptSequence.next()),
                saved.getOrderId(), actor.userId()));
        audit.record(new SalesAuditPort.AuditRecord(actor.userId(), "SALE.COMPLETED", "ORDER", saved.getOrderId(), saved.getLocationId(), null,
                List.of(new SalesAuditPort.Change("orderStatus", "PENDING", "COMPLETED"), new SalesAuditPort.Change("totalAmount", null, String.valueOf(saved.getTotalAmount().amount())), new SalesAuditPort.Change("amountPaid", null, String.valueOf(settled.amountPaid())), new SalesAuditPort.Change("amountDue", null, String.valueOf(settled.amountDue())))));
        return new SaleResult(OrderSupport.toResult(saved), receipt.getReceiptNumber().value(),
                receipt.getIssuedAt(), settled.amountPaid(), settled.amountDue(), customer.customerName(),
                location.locationName(), c.payments());
    }
}
