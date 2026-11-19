package tsgp.yah_enterprise.sales.domain.repository;

/** One group of a sales summary: the employee id, location id or day, and the completed orders in it. */
public record SummaryRow(String key, long orderCount, long totalAmount) { }
