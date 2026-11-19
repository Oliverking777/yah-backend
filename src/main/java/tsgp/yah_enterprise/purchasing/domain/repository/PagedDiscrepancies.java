package tsgp.yah_enterprise.purchasing.domain.repository;

import tsgp.yah_enterprise.purchasing.domain.model.PurchaseDiscrepancy;

import java.util.List;

public record PagedDiscrepancies(List<PurchaseDiscrepancy> items, long total) { }
