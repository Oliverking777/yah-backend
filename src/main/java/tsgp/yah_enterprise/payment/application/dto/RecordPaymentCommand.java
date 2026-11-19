package tsgp.yah_enterprise.payment.application.dto;

import tsgp.yah_enterprise.payment.domain.model.PaymentMethod;

import java.time.Instant;
import java.util.UUID;

/** paymentId is the client's UUID for offline work (or null). locationId null means the user's own location. */
public record RecordPaymentCommand(UUID paymentId, UUID customerId, UUID locationId, long amount,
                                   PaymentMethod paymentMethod, String reference, Instant receivedAt,
                                   PaymentActor actor) { }
