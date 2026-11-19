package tsgp.yah_enterprise.transfer.presentation.dto;

import tsgp.yah_enterprise.transfer.application.dto.InTransitResult;

import java.util.List;
import java.util.UUID;

public record InTransitResponse(List<Line> lines) {

    public record Line(UUID destinationLocationId, String destinationLocationName, UUID productId,
                       String productName, long baseQuantity) { }

    public static InTransitResponse from(InTransitResult r) {
        return new InTransitResponse(r.lines().stream()
                .map(l -> new Line(l.destinationLocationId(), l.destinationLocationName(), l.productId(),
                        l.productName(), l.baseQuantity()))
                .toList());
    }
}
