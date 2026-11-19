package tsgp.yah_enterprise.sales.infrastructure.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.pricing.application.dto.ApplicablePriceResult;
import tsgp.yah_enterprise.pricing.application.port.in.GetApplicablePriceUseCase;
import tsgp.yah_enterprise.pricing.domain.exception.ApplicablePriceNotFoundException;
import tsgp.yah_enterprise.sales.domain.port.SalesPricingPort;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Sales reads prices through Pricing's public use case only. No price means an empty Optional. */
@Component
@RequiredArgsConstructor
public class SalesPricingAdapter implements SalesPricingPort {

    private final GetApplicablePriceUseCase getApplicablePriceUseCase;

    @Override
    public Optional<SalePrice> applicablePrice(UUID productId, UUID unitId, int quantity, Instant at) {
        try {
            ApplicablePriceResult r = getApplicablePriceUseCase.getApplicablePrice(productId, unitId, quantity, at);
            return Optional.of(new SalePrice(r.priceId(), r.unitPrice()));
        } catch (ApplicablePriceNotFoundException e) {
            return Optional.empty();
        }
    }
}
