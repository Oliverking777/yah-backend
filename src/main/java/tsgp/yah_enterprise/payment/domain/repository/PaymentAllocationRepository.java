package tsgp.yah_enterprise.payment.domain.repository;

import tsgp.yah_enterprise.payment.domain.model.PaymentAllocation;

import java.util.List;
import java.util.UUID;

public interface PaymentAllocationRepository {

    List<PaymentAllocation> saveAll(List<PaymentAllocation> allocations);

    List<PaymentAllocation> findByPaymentId(UUID paymentId);

    List<PaymentAllocation> findBySaleId(UUID saleId);
}
