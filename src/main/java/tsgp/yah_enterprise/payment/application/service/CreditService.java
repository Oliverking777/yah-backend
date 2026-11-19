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

import java.util.UUID;

/** Credit requests and their approval by a Manager or Owner who is not the requester. */
@Service
@Transactional
public class CreditService implements RequestCreditUseCase, DecideCreditUseCase, GetCreditApprovalUseCase,
        ListCreditApprovalsUseCase {

    private final CreditApprovalRepository approvals;
    private final PaymentAuditPort audit;
    private final ReceivableRepository receivables;
    private final PaymentCustomerDirectoryPort customers;

    /** Kept for callers that do not audit (the unit tests). The Spring constructor is the next one. */
    public CreditService(CreditApprovalRepository approvals, ReceivableRepository receivables,
                         PaymentCustomerDirectoryPort customers) {
        this(approvals, receivables, customers, PaymentAuditPort.NOOP);
    }

    @Autowired
    public CreditService(CreditApprovalRepository approvals, ReceivableRepository receivables,
                         PaymentCustomerDirectoryPort customers,
                         PaymentAuditPort audit) {
        this.approvals = approvals;
        this.receivables = receivables;
        this.customers = customers;
        this.audit = audit;
    }

    @Override
    public CreditApprovalResult requestCredit(RequestCreditCommand command) {
        PaymentSupport.requireActor(command.actor());
        if (command.saleId() == null || command.customerId() == null) {
            throw new InvalidCreditRequestException("Sale and customer are required");
        }
        PaymentCustomerDirectoryPort.PaymentCustomer customer = customers.findCustomer(command.customerId())
                .filter(PaymentCustomerDirectoryPort.PaymentCustomer::active)
                .orElseThrow(() -> new PaymentCustomerNotUsableException(
                        "Customer " + command.customerId() + " does not exist or is inactive"));
        if (customer.walkIn()) {
            throw new CreditNotAllowedForCustomerException("The walk-in customer cannot buy on credit");
        }
        if (receivables.findBySaleId(command.saleId()).isPresent()) {
            throw new SaleAlreadySettledException("Sale " + command.saleId() + " is already settled");
        }
        if (approvals.findActiveBySale(command.saleId()).isPresent()) {
            throw new OpenCreditApprovalExistsException("Sale " + command.saleId()
                    + " already has an open credit approval");
        }
        CreditApproval approval = CreditApproval.request(command.saleId(), command.customerId(),
                Money.of(command.requestedAmount()), command.reason(), command.actor().userId());
        CreditApproval saved = approvals.save(approval);
        audit.record(new PaymentAuditPort.AuditRecord(command.actor().userId(), "CREDIT.REQUESTED", "CREDIT_APPROVAL", saved.getCreditApprovalId(), null, command.reason(),
                List.of(new PaymentAuditPort.Change("requestedAmount", null, String.valueOf(command.requestedAmount())), new PaymentAuditPort.Change("saleId", null, String.valueOf(saved.getSaleId())))));
        return PaymentSupport.toResult(saved);
    }

    @Override
    public CreditApprovalResult approveCredit(ApproveCreditCommand command) {
        PaymentSupport.requireActor(command.actor());
        CreditApproval approval = lock(command.creditApprovalId());
        approval.approve(command.actor().userId(),
                command.approvedAmount() == null ? null : Money.of(command.approvedAmount()), command.note());
        CreditApproval saved = approvals.save(approval);
        audit.record(new PaymentAuditPort.AuditRecord(command.actor().userId(), "CREDIT.APPROVED", "CREDIT_APPROVAL", saved.getCreditApprovalId(), null, command.note(),
                List.of(new PaymentAuditPort.Change("approvalStatus", "REQUESTED", "APPROVED"), new PaymentAuditPort.Change("approvedAmount", null, String.valueOf(saved.getApprovedAmount() == null ? null : saved.getApprovedAmount().amount())))));
        return PaymentSupport.toResult(saved);
    }

    @Override
    public CreditApprovalResult rejectCredit(RejectCreditCommand command) {
        PaymentSupport.requireActor(command.actor());
        CreditApproval approval = lock(command.creditApprovalId());
        approval.reject(command.actor().userId(), command.note());
        CreditApproval saved = approvals.save(approval);
        audit.record(new PaymentAuditPort.AuditRecord(command.actor().userId(), "CREDIT.REJECTED", "CREDIT_APPROVAL", saved.getCreditApprovalId(), null, command.note(),
                List.of(new PaymentAuditPort.Change("approvalStatus", "REQUESTED", "REJECTED"))));
        return PaymentSupport.toResult(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CreditApprovalResult getCreditApproval(UUID creditApprovalId) {
        return PaymentSupport.toResult(approvals.findById(creditApprovalId)
                .orElseThrow(() -> notFound(creditApprovalId)));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<CreditApprovalResult> listCreditApprovals(CreditPageRequest r) {
        PageSlice<CreditApproval> slice = approvals.search(r.status(), r.customerId(), r.page(), r.size());
        return PagedResult.of(slice.items().stream().map(PaymentSupport::toResult).toList(), r.page(), r.size(),
                slice.total());
    }

    private CreditApproval lock(UUID id) {
        return approvals.findByIdForUpdate(id).orElseThrow(() -> notFound(id));
    }

    private static CreditApprovalNotFoundException notFound(UUID id) {
        return new CreditApprovalNotFoundException("Credit approval " + id + " not found");
    }
}
