package tsgp.yah_enterprise.reports.infrastructure.integration;

import tsgp.yah_enterprise.reports.domain.model.SalesGroupBy;
import tsgp.yah_enterprise.reports.domain.port.ReportPaymentPort;
import tsgp.yah_enterprise.reports.domain.valueobject.LocationScope;
import tsgp.yah_enterprise.reports.domain.valueobject.ReportPeriod;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.payment.application.port.in.GetDebtRowsUseCase;
import tsgp.yah_enterprise.payment.application.port.in.GetOutstandingTotalsUseCase;
import tsgp.yah_enterprise.payment.application.port.in.GetPaymentTotalsUseCase;
import tsgp.yah_enterprise.payment.domain.repository.TotalsGroupBy;

import java.util.List;
import java.util.UUID;

/** Reports reads Payment only through its use cases. */
@Component
public class ReportPaymentAdapter implements ReportPaymentPort {

    private final GetPaymentTotalsUseCase collected;
    private final GetOutstandingTotalsUseCase outstanding;
    private final GetDebtRowsUseCase debts;

    public ReportPaymentAdapter(GetPaymentTotalsUseCase collected, GetOutstandingTotalsUseCase outstanding,
                                GetDebtRowsUseCase debts) {
        this.collected = collected;
        this.outstanding = outstanding;
        this.debts = debts;
    }

    @Override
    public List<AmountByKey> collectedTotals(ReportPeriod p, LocationScope s, SalesGroupBy by) {
        return collected.getPaymentTotals(p.start(), p.end(), s.idsOrNull(), TotalsGroupBy.valueOf(by.name()),
                p.zone().getId()).stream().map(r -> new AmountByKey(r.key(), r.amount())).toList();
    }

    @Override
    public List<AmountByKey> outstandingTotals(ReportPeriod p, LocationScope s, SalesGroupBy by) {
        return outstanding.getOutstandingTotals(p.start(), p.end(), s.idsOrNull(),
                TotalsGroupBy.valueOf(by.name()), p.zone().getId()).stream()
                .map(r -> new AmountByKey(r.key(), r.amount())).toList();
    }

    @Override
    public List<DebtLine> debtRows(ReportPeriod p, LocationScope s, UUID customerId, boolean onlyOutstanding) {
        return debts.getDebtRows(p.start(), p.end(), s.idsOrNull(), customerId, onlyOutstanding).stream()
                .map(r -> new DebtLine(r.customerId(), r.locationId(), r.amountDue(), r.amountPaid())).toList();
    }
}
