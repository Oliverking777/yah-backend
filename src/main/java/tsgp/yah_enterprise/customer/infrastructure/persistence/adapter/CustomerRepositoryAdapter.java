package tsgp.yah_enterprise.customer.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import tsgp.yah_enterprise.customer.domain.model.Customer;
import tsgp.yah_enterprise.customer.domain.model.CustomerStatus;
import tsgp.yah_enterprise.customer.domain.repository.CustomerRepository;
import tsgp.yah_enterprise.customer.infrastructure.persistence.mapper.CustomerMapper;
import tsgp.yah_enterprise.customer.infrastructure.persistence.repository.CustomerJpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository jpa;
    private final CustomerMapper mapper;

    @Override
    public Customer save(Customer customer) {
        return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(customer)));
    }

    @Override
    public Optional<Customer> findById(UUID customerId) {
        return jpa.findById(customerId).map(mapper::toDomain);
    }

    @Override
    public List<Customer> findAllByIds(Collection<UUID> customerIds) {
        return jpa.findAllByCustomerIdIn(customerIds).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Customer> findByCode(String code) {
        return jpa.findByCustomerCode(code).map(mapper::toDomain);
    }

    @Override
    public List<Customer> findAll(int page, int size, CustomerStatus status) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("customerName").ascending());
        if (status != null) {
            return jpa.findAllByCustomerStatus(status, pageable).getContent()
                    .stream().map(mapper::toDomain).toList();
        }
        return jpa.findAll(pageable).getContent().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Customer> search(String term, int page, int size) {
        return jpa.search(term, PageRequest.of(page, size)).getContent()
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long count(CustomerStatus status) {
        if (status != null) return jpa.countByCustomerStatus(status);
        return jpa.count();
    }

    @Override
    public long countSearch(String term) {
        return jpa.search(term, PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
    }
}
