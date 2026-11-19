package tsgp.yah_enterprise.reports.domain.model;

import tsgp.yah_enterprise.reports.domain.exception.InvalidReportRequestException;

public enum ReportName {
    SALES("sales"), STOCK("stock"), PURCHASING("purchasing"), TRANSFERS("transfers"), DEBTS("debts"),
    TOP_PRODUCTS("top-products");

    private final String path;

    ReportName(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

    public static ReportName fromPath(String value) {
        for (ReportName n : values()) {
            if (n.path.equalsIgnoreCase(value == null ? "" : value.trim())) {
                return n;
            }
        }
        throw new InvalidReportRequestException("Unknown report: " + value
                + ". Use sales, stock, purchasing, transfers, debts or top-products");
    }
}
