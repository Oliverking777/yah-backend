package tsgp.yah_enterprise.payment.application.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import tsgp.yah_enterprise.payment.domain.port.PaymentAuditPort;
import tsgp.yah_enterprise.payment.application.dto.*;
import tsgp.yah_enterprise.payment.domain.exception.*;
import tsgp.yah_enterprise.payment.domain.model.*;
import tsgp.yah_enterprise.payment.domain.port.*;
import tsgp.yah_enterprise.payment.domain.valueobject.Money;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.payment.application.port.in.*;
import tsgp.yah_enterprise.payment.domain.repository.*;
import tsgp.yah_enterprise.payment.domain.service.AllocationPolicy;
import tsgp.yah_enterprise.payment.domain.valueobject.PaymentNumber;

import java.util.*;

/**
 * Receiving, allocating and reversing payments, plus the balance and debt reads. Debt is never edited:
 * it is derived from receivables and active allocations (F9).
 */
@Service
@Transactional
public class PaymentService implements RecordPaymentUseCase, GetPaymentUseCase, ListPaymentsUseCase,
        AllocatePaymentUseCase, ReversePaymentUseCase, GetCustomerBalanceUseCase, ListDebtsUseCase {

    private final PaymentRepository payments;
    private final PaymentAuditPort audit;
    private final ReceivableRepository receivables;
    private final PaymentAllocationRepository allocations;
    private final PaymentNumberSequence numberSequence;
    private final PaymentCustomerDirectoryPort customers;
    private final PaymentSupport support;

    /** Kept for callers that do not audit (the unit tests). The Spring constructor is the next one. */
    public PaymentService(PaymentRepository payments, ReceivableRepository receivables,
                          PaymentAllocationRepository allocations, PaymentNumberSequence numberSequence,
                          PaymentCustomerDirectoryPort customers, PaymentLocationDirectoryPort locations) {
        this(payments, receivables, allocations, numberSequence, customers, locations, PaymentAuditPort.NOOP);
    }

    @Autowired
    public PaymentService(PaymentRepository payments, ReceivableRepository receivables,
                          PaymentAllocationRepository allocations, PaymentNumberSequence numberSequence,
                          PaymentCustomerDirectoryPort customers, PaymentLocationDirectoryPort locations,
                          PaymentAuditPort audit) {
        this.payments = payments;
        this.receivables = receivables;
        this.allocations = allocations;
        this.numberSequence = numberSequence;
        this.customers = customers;
        this.support = new PaymentSupport(locations);
        this.audit = audit;
    }

    @Override
    public PaymentResult recordPayment(RecordPaymentCommand command) {
        if (command == null) {
            throw new InvalidPaymentException("The payment is required");
        }
        PaymentActor actor = command.actor();
        PaymentSupport.requireActor(actor);
        if (command.paymentId() != null) {
            Optional<Payment> existing = payments.findById(command.paymentId());
            if (existing.isPresent()) {
                if (!actor.userId().equals(existing.get().getReceivedBy())) {
                    throw new PaymentIdAlreadyUsedException("Payment id " + command.paymentId() + " is already used");
                }
                return PaymentSupport.toResult(existing.get());
            }
        }
        if (command.customerId() == null) {
            throw new InvalidPaymentException("The customer is required");
        }
        UUID locationId = command.locationId() != null ? command.locationId()
                : support.assignedLocation(actor.userId()).orElseThrow(() ->
                        new InvalidPaymentException("The location is required"));
        support.requireAtLocation(actor, locationId);
        support.requireUsableLocation(locationId);
        requireUsableCustomer(command.customerId());
        if (command.paymentMethod() == PaymentMethod.MOBILE_MONEY && command.reference() != null
                && payments.existsByMethodAndReference(PaymentMethod.MOBILE_MONEY, command.reference().trim())) {
            throw new DuplicatePaymentReferenceException("Reference " + command.reference().trim()
                    + " was already used");
        }
        Payment payment = Payment.record(command.paymentId(), PaymentNumber.fromSequence(numberSequence.next()),
                command.customerId(), locationId, Money.of(command.amount()), command.paymentMethod(),
                command.reference(), actor.userId(), command.receivedAt());
        Payment saved = payments.save(payment);
        audit.record(new PaymentAuditPort.AuditRecord(actor.userId(), "PAYMENT.RECORDED", "PAYMENT", saved.getPaymentId(), saved.getLocationId(), null,
                List.of(new PaymentAuditPort.Change("amount", null, String.valueOf(saved.getAmount().amount())), new PaymentAuditPort.Change("paymentMethod", null, String.valueOf(command.paymentMethod())), new PaymentAuditPort.Change("customerId", null, String.valueOf(saved.getCustomerId())))));
        return PaymentSupport.toResult(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResult getPayment(UUID paymentId, PaymentActor actor) {
        Payment payment = payments.findById(paymentId).orElseThrow(() -> notFound(paymentId));
        support.requireAtLocation(actor, payment.getLocationId());
        return PaymentSupport.toResult(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<PaymentResult> listPayments(PaymentPageRequest r) {
        UUID scope = support.resolveScope(r.actor(), r.locationId());
        PageSlice<Payment> slice = payments.search(r.customerId(), scope, r.status(), r.page(), r.size());
        return PagedResult.of(slice.items().stream().map(PaymentSupport::toResult).toList(), r.page(), r.size(),
                slice.total());
    }

    @Override
    public List<AllocationResult> allocatePayment(AllocatePaymentCommand command) {
        PaymentSupport.requireActor(command.actor());
        Payment payment = payments.findByIdForUpdate(command.paymentId())
                .orElseThrow(() -> notFound(command.paymentId()));
        support.requireAtLocation(command.actor(), payment.getLocationId());
        if (!payment.isRecorded()) {
            throw new PaymentAlreadyReversedException("Payment " + payment.getPaymentNumber() + " is reversed");
        }
        List<AllocationLine> lines = command.lines();
        Map<UUID, Receivable> touched = new LinkedHashMap<>();
        List<PaymentAllocation> created = new ArrayList<>();
        if (lines == null || lines.isEmpty()) {
            if (payment.unallocated().isZero()) {
                throw new PaymentOverAllocationException("Payment " + payment.getPaymentNumber()
                        + " has nothing left to allocate");
            }
            List<Receivable> open = receivables.findOpenByCustomerForUpdate(payment.getCustomerId());
            for (AllocationPolicy.Share share : AllocationPolicy.oldestFirst(payment.unallocated(), open)) {
                Receivable receivable = open.stream().filter(x -> x.getSaleId().equals(share.saleId()))
                        .findFirst().orElseThrow();
                apply(payment, receivable, share.amount(), created);
                touched.put(receivable.getSaleId(), receivable);
            }
            if (created.isEmpty()) {
                throw new ReceivableOverAllocationException("The customer has no debt to allocate this payment to");
            }
        } else {
            for (AllocationLine line : lines) {
                if (line == null || line.saleId() == null) {
                    throw new InvalidPaymentException("An allocation line needs a sale");
                }
                Receivable receivable = touched.get(line.saleId());
                if (receivable == null) {
                    receivable = receivables.findBySaleIdForUpdate(line.saleId()).orElseThrow(() ->
                            new ReceivableNotFoundException("Sale " + line.saleId() + " has no debt"));
                    touched.put(line.saleId(), receivable);
                }
                if (!receivable.getCustomerId().equals(payment.getCustomerId())) {
                    throw new AllocationCustomerMismatchException(
                            "The payment and the sale belong to different customers");
                }
                apply(payment, receivable, Money.of(line.amount()), created);
            }
        }
        touched.values().forEach(receivables::save);
        payments.save(payment);
        return allocations.saveAll(created).stream().map(PaymentSupport::toResult).toList();
    }

    @Override
    public PaymentResult reversePayment(ReversePaymentCommand command) {
        PaymentSupport.requireActor(command.actor());
        Payment payment = payments.findByIdForUpdate(command.paymentId())
                .orElseThrow(() -> notFound(command.paymentId()));
        support.requireAtLocation(command.actor(), payment.getLocationId());
        if (!payment.isRecorded()) {
            throw new PaymentAlreadyReversedException("Payment " + payment.getPaymentNumber()
                    + " is already reversed");
        }
        List<PaymentAllocation> active = allocations.findByPaymentId(payment.getPaymentId()).stream()
                .filter(PaymentAllocation::isActive).toList();
        List<Receivable> touched = new ArrayList<>();
        for (PaymentAllocation a : active) {
            Receivable receivable = receivables.findBySaleIdForUpdate(a.getSaleId()).orElseThrow(() ->
                    new ReceivableNotFoundException("Sale " + a.getSaleId() + " has no debt"));
            receivable.release(a.getAllocatedAmount());
            a.reverse();
            touched.add(receivable);
        }
        payment.reverse(command.actor().userId(), command.reason());
        touched.forEach(receivables::save);
        allocations.saveAll(active);
        Payment saved = payments.save(payment);
        audit.record(new PaymentAuditPort.AuditRecord(command.actor().userId(), "PAYMENT.REVERSED", "PAYMENT", saved.getPaymentId(), saved.getLocationId(), command.reason(),
                List.of(new PaymentAuditPort.Change("paymentStatus", "RECORDED", "REVERSED"), new PaymentAuditPort.Change("amount", String.valueOf(saved.getAmount().amount()), "0"))));
        return PaymentSupport.toResult(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerBalanceResult getBalance(UUID customerId) {
        PaymentCustomerDirectoryPort.PaymentCustomer customer = customers.findCustomer(customerId)
                .orElseThrow(() -> new PaymentCustomerNotFoundException("Customer " + customerId + " not found"));
        return new CustomerBalanceResult(customerId, customer.customerName(),
                receivables.outstandingOfCustomer(customerId).amount());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<DebtResult> listDebts(DebtPageRequest r) {
        UUID scope = support.resolveScope(r.actor(), r.locationId());
        PageSlice<Receivable> slice = receivables.searchOpen(r.customerId(), scope, r.page(), r.size());
        return PagedResult.of(slice.items().stream().map(PaymentSupport::toResult).toList(), r.page(), r.size(),
                slice.total());
    }

    private static void apply(Payment payment, Receivable receivable, Money part, List<PaymentAllocation> out) {
        payment.allocate(part);
        receivable.allocate(part);
        out.add(PaymentAllocation.create(payment.getPaymentId(), receivable.getSaleId(), part));
    }

    private void requireUsableCustomer(UUID customerId) {
        boolean usable = customers.findCustomer(customerId)
                .map(PaymentCustomerDirectoryPort.PaymentCustomer::active).orElse(false);
        if (!usable) {
            throw new PaymentCustomerNotUsableException("Customer " + customerId + " does not exist or is inactive");
        }
    }

    private static PaymentNotFoundException notFound(UUID paymentId) {
        return new PaymentNotFoundException("Payment " + paymentId + " not found");
    }
}
