package tsgp.yah_enterprise.sales.application.dto;

import java.util.List;

public record SalesSummaryResult(String groupBy, List<Row> rows, long totalOrders, long totalAmount) {

    public record Row(String key, long orderCount, long totalAmount) { }

    public SalesSummaryResult {
        rows = List.copyOf(rows);
    }
}
