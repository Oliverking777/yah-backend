package tsgp.yah_enterprise.dashboard.infrastructure.integration;

import tsgp.yah_enterprise.dashboard.domain.valueobject.AlertThresholds;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/** Alert thresholds from yah.dashboard.* properties; every one has a default. */
@Configuration
public class DashboardConfig {

    @Bean
    public AlertThresholds alertThresholds(
            @Value("${yah.dashboard.order-warning-hours:4}") long orderHours,
            @Value("${yah.dashboard.credit-warning-hours:2}") long creditHours,
            @Value("${yah.dashboard.adjustment-warning-hours:24}") long adjustmentHours,
            @Value("${yah.dashboard.transfer-receipt-warning-hours:48}") long receiptWarnHours,
            @Value("${yah.dashboard.transfer-receipt-critical-hours:96}") long receiptCritHours,
            @Value("${yah.dashboard.transfer-discrepancy-days:30}") long transferDiscrepancyDays,
            @Value("${yah.dashboard.purchase-delivery-due-days:7}") long purchaseDueDays,
            @Value("${yah.dashboard.purchase-discrepancy-days:30}") long purchaseDiscrepancyDays) {
        return new AlertThresholds(Duration.ofHours(orderHours), Duration.ofHours(creditHours),
                Duration.ofHours(adjustmentHours), Duration.ofHours(receiptWarnHours),
                Duration.ofHours(receiptCritHours), Duration.ofDays(transferDiscrepancyDays),
                Duration.ofDays(purchaseDueDays), Duration.ofDays(purchaseDiscrepancyDays));
    }
}
