package tsgp.yah_enterprise.sales.application.dto;

import java.util.List;
import java.util.UUID;

/** payments may be empty when the whole sale goes on an approved credit. discounts are optional, one per line. */
public record CompleteSaleCommand(UUID orderId, List<PaymentLineCommand> payments, UUID creditApprovalId,
                                  OrderActor actor, List<DiscountCommand> discounts) {

    public CompleteSaleCommand(UUID orderId, List<PaymentLineCommand> payments, UUID creditApprovalId,
                               OrderActor actor) {
        this(orderId, payments, creditApprovalId, actor, null);
    }
}
