package tsgp.yah_enterprise.payment.application.service;

import tsgp.yah_enterprise.payment.application.dto.*;
import tsgp.yah_enterprise.payment.domain.exception.*;
import tsgp.yah_enterprise.payment.domain.model.*;
import tsgp.yah_enterprise.payment.domain.port.*;
import tsgp.yah_enterprise.payment.domain.valueobject.Money;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.payment.application.port.in.SettleSaleUseCase;
import tsgp.yah_enterprise.payment.domain.repository.*;
import tsgp.yah_enterprise.payment.domain.service.SettlementPolicy;
import tsgp.yah_enterprise.payment.domain.valueobject.PaymentNumber;

import java.util.*;

/**
 * Called by Sales when it completes a sale, inside Sales' transaction. Everything is checked before anything is
 * saved: paid in full, or the rest on an approved credit that is then consumed. The payments are allocated to the
 * sale at once and a receivable is created, even when the sale is fully paid.
 */
@Service
@Transactional
public class SettlementService implements SettleSaleUseCase {

    private final PaymentRepository payments;
    private final ReceivableRepository receivables;
    private final PaymentAllocationRepository allocations;
    private final CreditApprovalRepository approvals;
    private final PaymentNumberSequence numberSequence;
    private final PaymentCustomerDirectoryPort customers;
    private final PaymentLocationDirectoryPort locations;

    public SettlementService(PaymentRepository payments, ReceivableRepository receivables,
                             PaymentAllocationRepository allocations, CreditApprovalRepository approvals,
                             PaymentNumberSequence numberSequence, PaymentCustomerDirectoryPort customers,
                             PaymentLocationDirectoryPort locations) {
        this.payments = payments;
        this.receivables = receivables;
        this.allocations = allocations;
        this.approvals = approvals;
        this.numberSequence = numberSequence;
        this.customers = customers;
        this.locations = locations;
    }

    @Override
    public SettlementResult settleSale(SettleSaleCommand c) {
        if (c == null || c.saleId() == null || c.customerId() == null || c.locationId() == null
                || c.userId() == null) {
            throw new InvalidPaymentException("Sale, customer, location and user are required");
        }
        Money total = Money.of(c.totalAmount());
        if (receivables.findBySaleId(c.saleId()).isPresent()) {
            throw new SaleAlreadySettledException("Sale " + c.saleId() + " is already settled");
        }
        PaymentCustomerDirectoryPort.PaymentCustomer customer = customers.findCustomer(c.customerId())
                .filter(PaymentCustomerDirectoryPort.PaymentCustomer::active)
                .orElseThrow(() -> new PaymentCustomerNotUsableException(
                        "Customer " + c.customerId() + " does not exist or is inactive"));
        if (!locations.isActive(c.locationId())) {
            throw new PaymentLocationNotUsableException("Location " + c.locationId() + " does not exist or is inactive");
        }

        List<Payment> recorded = new ArrayList<>();
        Set<String> references = new HashSet<>();
        Money paid = Money.ZERO;
        for (SettlementPayment p : c.payments() == null ? List.<SettlementPayment>of() : c.payments()) {
            if (p == null) {
                throw new InvalidPaymentException("A payment line cannot be empty");
            }
            Payment payment = Payment.record(null, PaymentNumber.fromSequence(numberSequence.next()),
                    c.customerId(), c.locationId(), Money.of(p.amount()), p.paymentMethod(), p.reference(),
                    c.userId(), c.paidAt());
            if (payment.getReference() != null && (!references.add(payment.getPaymentMethod() + ":"
                    + payment.getReference()) || payments.existsByMethodAndReference(payment.getPaymentMethod(),
                    payment.getReference()))) {
                throw new DuplicatePaymentReferenceException("Reference " + payment.getReference()
                        + " was already used");
            }
            recorded.add(payment);
            paid = paid.plus(payment.getAmount());
        }

        CreditApproval approval = c.creditApprovalId() == null ? null
                : approvals.findByIdForUpdate(c.creditApprovalId()).orElseThrow(() ->
                        new CreditApprovalNotFoundException("Credit approval " + c.creditApprovalId() + " not found"));
        Money due = SettlementPolicy.amountDue(c.saleId(), c.customerId(), customer.walkIn(), total, paid, approval);

        // Everything is valid: now change and save.
        Receivable receivable = Receivable.open(c.saleId(), c.customerId(), c.locationId(), total);
        List<PaymentAllocation> created = new ArrayList<>();
        for (Payment payment : recorded) {
            payment.allocate(payment.getAmount());
            receivable.allocate(payment.getAmount());
            created.add(PaymentAllocation.create(payment.getPaymentId(), c.saleId(), payment.getAmount()));
        }
        if (due.isPositive()) {
            approval.consume();
            approvals.save(approval);
        }
        recorded.forEach(payments::save);
        receivables.save(receivable);
        allocations.saveAll(created);
        return new SettlementResult(paid.amount(), due.amount());
    }
}
