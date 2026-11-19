package tsgp.yah_enterprise.payment.domain.repository;

import tsgp.yah_enterprise.payment.domain.model.CreditApproval;
import tsgp.yah_enterprise.payment.domain.model.CreditApprovalStatus;

import java.util.Optional;
import java.util.UUID;

public interface CreditApprovalRepository {

    CreditApproval save(CreditApproval approval);

    Optional<CreditApproval> findById(UUID creditApprovalId);

    Optional<CreditApproval> findByIdForUpdate(UUID creditApprovalId);

    /** The REQUESTED or APPROVED approval of a sale, if any. */
    Optional<CreditApproval> findActiveBySale(UUID saleId);

    PageSlice<CreditApproval> search(CreditApprovalStatus status, UUID customerId, int page, int size);
}
