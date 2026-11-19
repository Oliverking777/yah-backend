package tsgp.yah_enterprise.dashboard.domain.valueobject;

import java.time.Duration;

/**
 * Alert thresholds. They are properties (yah.dashboard.*), never constants in code.
 * purchaseDeliveryDue: how long after ordering a purchase without an expected delivery date counts as late.
 * The two windows: how long a delivery with rejected or missing goods stays on the alert list.
 */
public record AlertThresholds(Duration orderWarning, Duration creditWarning, Duration adjustmentWarning,
                              Duration transferReceiptWarning, Duration transferReceiptCritical,
                              Duration discrepancyTransferWindow, Duration purchaseDeliveryDue,
                              Duration discrepancyPurchaseWindow) {

    public static AlertThresholds defaults() {
        return new AlertThresholds(Duration.ofHours(4), Duration.ofHours(2), Duration.ofHours(24),
                Duration.ofHours(48), Duration.ofHours(96), Duration.ofDays(30),
                Duration.ofDays(7), Duration.ofDays(30));
    }
}
