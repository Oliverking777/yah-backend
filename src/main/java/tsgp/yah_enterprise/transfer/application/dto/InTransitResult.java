package tsgp.yah_enterprise.transfer.application.dto;

import java.util.List;

/** Goods in transit, derived from SUBMITTED transfers. It is not a stock balance (F4). */
public record InTransitResult(List<InTransitLine> lines) {
    public InTransitResult {
        lines = List.copyOf(lines);
    }
}