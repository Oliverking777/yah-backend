package tsgp.yah_enterprise.pricing.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.pricing.domain.exception.PriceRangeOverlapException;
import tsgp.yah_enterprise.pricing.domain.model.PriceStatus;
import tsgp.yah_enterprise.pricing.domain.model.ProductPrice;
import tsgp.yah_enterprise.pricing.domain.repository.ProductPriceRepository;
import tsgp.yah_enterprise.pricing.infrastructure.persistence.mapper.ProductPriceMapper;
import tsgp.yah_enterprise.pricing.infrastructure.persistence.repository.ProductPriceJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductPriceRepositoryAdapter implements ProductPriceRepository {

    private static final String OVERLAP_CONSTRAINT = "ex_product_prices_no_overlap";

    /** Newest first; createdAt breaks ties between rows that start at the same moment. */
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("effectiveFrom"), Sort.Order.desc("createdAt"));

    private final ProductPriceJpaRepository jpaRepository;
    private final ProductPriceMapper mapper;

    @Override
    public ProductPrice save(ProductPrice price) {
        try {
            // saveAndFlush so a concurrent overlapping price fails here, not later at commit time
            return mapper.toDomain(jpaRepository.saveAndFlush(mapper.toEntity(price)));
        } catch (DataIntegrityViolationException e) {
            if (isOverlapViolation(e)) {
                throw new PriceRangeOverlapException(price.getQuantityRange());
            }
            throw e;                                   // any other violation is a real bug: do not hide it
        }
    }

    @Override
    public Optional<ProductPrice> findById(UUID priceId) {
        return jpaRepository.findById(priceId).map(mapper::toDomain);
    }

    @Override
    public List<ProductPrice> findActiveByProductAndUnit(UUID productId, UUID unitId) {
        return jpaRepository.findByProductIdAndUnitIdAndPriceStatus(productId, unitId, PriceStatus.ACTIVE)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ProductPrice> findActiveByProduct(UUID productId) {
        return jpaRepository.findByProductIdAndPriceStatus(productId, PriceStatus.ACTIVE)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ProductPrice> findByProduct(UUID productId, UUID unitId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, NEWEST_FIRST);
        var result = unitId == null
                ? jpaRepository.findByProductId(productId, pageRequest)
                : jpaRepository.findByProductIdAndUnitId(productId, unitId, pageRequest);
        return result.getContent().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByProduct(UUID productId, UUID unitId) {
        return unitId == null
                ? jpaRepository.countByProductId(productId)
                : jpaRepository.countByProductIdAndUnitId(productId, unitId);
    }

    private static boolean isOverlapViolation(DataIntegrityViolationException e) {
        String message = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
        return message != null && message.contains(OVERLAP_CONSTRAINT);
    }
}
