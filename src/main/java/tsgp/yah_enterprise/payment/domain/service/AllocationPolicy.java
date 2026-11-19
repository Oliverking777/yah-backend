package tsgp.yah_enterprise.payment.domain.service;

import tsgp.yah_enterprise.payment.domain.model.Receivable;
import tsgp.yah_enterprise.payment.domain.valueobject.Money;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Spreads an amount over the oldest debts first. */
public final class AllocationPolicy {

    public record Share(UUID saleId, Money amount) {
    }

    private AllocationPolicy() {
    }

    public static List<Share> oldestFirst(Money available, List<Receivable> receivables) {
        List<Receivable> sorted = new ArrayList<>(receivables);
        sorted.sort(Comparator.comparing(Receivable::getCreatedAt));
        List<Share> shares = new ArrayList<>();
        Money left = available;
        for (Receivable r : sorted) {
            if (left.isZero()) {
                break;
            }
            Money owed = r.outstanding();
            if (owed.isZero()) {
                continue;
            }
            Money part = owed.isGreaterThan(left) ? left : owed;
            shares.add(new Share(r.getSaleId(), part));
            left = left.minus(part);
        }
        return shares;
    }
}
