package tsgp.yah_enterprise.sales.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.payment.application.dto.SettleSaleCommand;
import tsgp.yah_enterprise.payment.application.dto.SettlementPayment;
import tsgp.yah_enterprise.payment.application.dto.SettlementResult;
import tsgp.yah_enterprise.payment.application.port.in.SettleSaleUseCase;
import tsgp.yah_enterprise.payment.domain.exception.PaymentDomainException;
import tsgp.yah_enterprise.payment.domain.model.PaymentMethod;
import tsgp.yah_enterprise.sales.domain.exception.SaleSettlementRejectedException;
import tsgp.yah_enterprise.sales.domain.port.SalesSettlementPort;

/**
 * Sales settles the money through Payment's inbound port only, inside Sales' transaction. Payment's exceptions
 * never escape: a refusal becomes SaleSettlementRejectedException carrying the reason.
 */
@Component
@RequiredArgsConstructor
public class SalesSettlementAdapter implements SalesSettlementPort {

    private final SettleSaleUseCase settleSaleUseCase;

    @Override
    public SaleSettlement settle(SettlementRequest r) {
        try {
            SettlementResult result = settleSaleUseCase.settleSale(new SettleSaleCommand(r.saleId(), r.customerId(),
                    r.locationId(), r.totalAmount(),
                    r.payments().stream().map(p -> new SettlementPayment(PaymentMethod.valueOf(
                            p.paymentMethod().name()), p.amount(), p.reference())).toList(),
                    r.creditApprovalId(), r.userId(), r.paidAt()));
            return new SaleSettlement(result.amountPaid(), result.amountDue());
        } catch (PaymentDomainException e) {
            throw new SaleSettlementRejectedException(e.getMessage());
        }
    }
}
