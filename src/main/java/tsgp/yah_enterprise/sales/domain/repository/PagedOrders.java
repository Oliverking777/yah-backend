package tsgp.yah_enterprise.sales.domain.repository;

import tsgp.yah_enterprise.sales.domain.model.Order;

import java.util.List;

public record PagedOrders(List<Order> items, long total) { }
