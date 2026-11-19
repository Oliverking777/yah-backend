package tsgp.yah_enterprise.payment.application.dto;

import tsgp.yah_enterprise.payment.domain.model.CreditApprovalStatus;

import java.util.UUID;

public record CreditPageRequest(int page, int size, CreditApprovalStatus status, UUID customerId) {

    public CreditPageRequest {
        page = PageLimits.page(page);
        size = PageLimits.size(size);
    }
}
