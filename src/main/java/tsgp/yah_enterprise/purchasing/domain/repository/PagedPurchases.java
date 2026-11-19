package tsgp.yah_enterprise.purchasing.domain.repository;

import tsgp.yah_enterprise.purchasing.domain.model.Purchase;

import java.util.List;

public record PagedPurchases(List<Purchase> items, long total) { }
