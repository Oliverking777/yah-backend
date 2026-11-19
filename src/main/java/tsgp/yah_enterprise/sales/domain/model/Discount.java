package tsgp.yah_enterprise.sales.domain.model;

import tsgp.yah_enterprise.sales.domain.valueobject.Money;

import java.util.UUID;

/** A discount a Manager or Owner gives to one line when validating. */
public record Discount(UUID productId, UUID unitId, Money amount) { }
