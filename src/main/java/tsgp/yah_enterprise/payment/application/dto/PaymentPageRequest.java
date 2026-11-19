package tsgp.yah_enterprise.payment.application.dto;

import tsgp.yah_enterprise.payment.domain.model.PaymentStatus;

import java.util.UUID;

public record PaymentPageRequest(int page, int size, UUID customerId, UUID locationId, PaymentStatus status,
                                 PaymentActor actor) {

    public PaymentPageRequest {
        page = PageLimits.page(page);
        size = PageLimits.size(size);
    }
}
