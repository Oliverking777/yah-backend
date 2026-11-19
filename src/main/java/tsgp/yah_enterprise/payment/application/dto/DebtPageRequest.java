package tsgp.yah_enterprise.payment.application.dto;

import java.util.UUID;

public record DebtPageRequest(int page, int size, UUID customerId, UUID locationId, PaymentActor actor) {

    public DebtPageRequest {
        page = PageLimits.page(page);
        size = PageLimits.size(size);
    }
}
