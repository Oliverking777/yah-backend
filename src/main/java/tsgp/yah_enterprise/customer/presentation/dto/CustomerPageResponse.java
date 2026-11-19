package tsgp.yah_enterprise.customer.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import tsgp.yah_enterprise.customer.application.dto.CustomerPageResult;

import java.util.List;

@Schema(description = "One page of customers")
public record CustomerPageResponse(
        List<CustomerResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static CustomerPageResponse from(CustomerPageResult r) {
        return new CustomerPageResponse(
                r.content().stream().map(CustomerResponse::from).toList(),
                r.page(), r.size(), r.totalElements(), r.totalPages(), r.hasNext(), r.hasPrevious());
    }
}
