package tsgp.yah_enterprise.customer.domain.repository;

import tsgp.yah_enterprise.customer.domain.model.Customer;
import tsgp.yah_enterprise.customer.domain.model.CustomerStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID customerId);

    List<Customer> findAllByIds(Collection<UUID> customerIds);

    Optional<Customer> findByCode(String code);

    List<Customer> findAll(int page, int size, CustomerStatus status);

    List<Customer> search(String term, int page, int size);

    long count(CustomerStatus status);

    long countSearch(String term);
}
