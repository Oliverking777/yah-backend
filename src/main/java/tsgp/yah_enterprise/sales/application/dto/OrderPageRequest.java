package tsgp.yah_enterprise.sales.application.dto;

import tsgp.yah_enterprise.sales.domain.model.OrderStatus;

import java.time.Instant;
import java.util.UUID;

/** Paging is clamped. from is inclusive, to is exclusive. */
public record OrderPageRequest(int page, int size, UUID locationId, OrderStatus status, UUID customerId,
                               UUID createdBy, Instant from, Instant to, OrderActor actor) {

    public OrderPageRequest {
        page = PageLimits.page(page);
        size = PageLimits.size(size);
    }
}
