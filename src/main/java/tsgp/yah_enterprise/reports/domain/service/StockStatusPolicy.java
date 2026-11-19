package tsgp.yah_enterprise.reports.domain.service;

import tsgp.yah_enterprise.reports.domain.model.StockStatus;

public class StockStatusPolicy {

    /** OUT_OF_STOCK when nothing is left, LOW when at or under the configured threshold, otherwise OK. */
    public StockStatus statusOf(long quantity, Long lowStockThreshold) {
        if (quantity <= 0) {
            return StockStatus.OUT_OF_STOCK;
        }
        if (lowStockThreshold != null && quantity <= lowStockThreshold) {
            return StockStatus.LOW;
        }
        return StockStatus.OK;
    }
}
