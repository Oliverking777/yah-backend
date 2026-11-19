package tsgp.yah_enterprise.sales.domain.model;

import tsgp.yah_enterprise.sales.domain.valueobject.Money;

import java.util.UUID;

/** The price pricing gave for one line: the row used (for audit) and the unit price. */
public record PriceQuote(UUID priceId, Money unitPrice) { }
