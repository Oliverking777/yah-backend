package tsgp.yah_enterprise.sales.domain.repository;

/** One group of completed sales. key is "ALL", a location id or a local day (yyyy-MM-dd). gross = sum of order subtotals. */
public record SalesTotalsRow(String key, long completedSalesCount, long grossAmount, long discountAmount) {
    public long netAmount() {
        return grossAmount - discountAmount;
    }
}
