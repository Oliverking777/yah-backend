package tsgp.yah_enterprise.sales.application.dto;

import tsgp.yah_enterprise.sales.domain.model.SalesPaymentMethod;

public record PaymentLineCommand(SalesPaymentMethod paymentMethod, long amount, String reference) { }
