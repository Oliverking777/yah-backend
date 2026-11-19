package tsgp.yah_enterprise.payment.application.dto;

import tsgp.yah_enterprise.payment.domain.model.PaymentMethod;

public record SettlementPayment(PaymentMethod paymentMethod, long amount, String reference) { }
