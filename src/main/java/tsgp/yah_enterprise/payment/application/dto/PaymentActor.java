package tsgp.yah_enterprise.payment.application.dto;

import java.util.UUID;

/** Who is acting. anyLocation is true for ADMIN and OWNER; everyone else acts only at their assigned location. */
public record PaymentActor(UUID userId, boolean anyLocation) { }
