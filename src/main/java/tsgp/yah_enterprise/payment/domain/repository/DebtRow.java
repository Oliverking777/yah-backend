package tsgp.yah_enterprise.payment.domain.repository;

import java.util.UUID;

public record DebtRow(UUID customerId, UUID locationId, long amountDue, long amountPaid) {
    public long outstanding() {
        return amountDue - amountPaid;
    }
}
