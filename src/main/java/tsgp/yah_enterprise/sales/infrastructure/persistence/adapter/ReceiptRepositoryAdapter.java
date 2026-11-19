package tsgp.yah_enterprise.sales.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.sales.domain.model.Receipt;
import tsgp.yah_enterprise.sales.domain.repository.ReceiptRepository;
import tsgp.yah_enterprise.sales.infrastructure.persistence.mapper.OrderMapper;
import tsgp.yah_enterprise.sales.infrastructure.persistence.repository.ReceiptJpaRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReceiptRepositoryAdapter implements ReceiptRepository {

    private final ReceiptJpaRepository jpa;
    private final OrderMapper mapper;

    @Override
    public Receipt save(Receipt receipt) {
        return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(receipt)));
    }

    @Override
    public Optional<Receipt> findByOrderId(UUID orderId) {
        return jpa.findByOrderId(orderId).map(mapper::toDomain);
    }
}
