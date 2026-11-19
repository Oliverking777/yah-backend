package tsgp.yah_enterprise.inventory.domain.repository;

import java.util.List;

public record StockBalancePage(List<StockBalanceRow> items, long total) { }
