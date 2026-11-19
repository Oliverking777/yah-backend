package tsgp.yah_enterprise.sales.application.dto;

import tsgp.yah_enterprise.sales.domain.repository.SummaryGroupBy;

import java.time.Instant;
import java.util.UUID;

public record SalesSummaryRequest(Instant from, Instant to, SummaryGroupBy groupBy, UUID locationId,
                                  OrderActor actor) { }
