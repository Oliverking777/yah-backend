package tsgp.yah_enterprise.purchasing.domain.repository;

import tsgp.yah_enterprise.purchasing.domain.model.GoodsReceipt;

import java.util.List;

public record PagedReceipts(List<GoodsReceipt> items, long total) { }
