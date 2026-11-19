package tsgp.yah_enterprise.customer.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tsgp.yah_enterprise.customer.domain.model.CustomerStatus;
import tsgp.yah_enterprise.customer.infrastructure.persistence.entity.CustomerJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID> {

    Optional<CustomerJpaEntity> findByCustomerCode(String customerCode);

    List<CustomerJpaEntity> findAllByCustomerIdIn(Iterable<UUID> ids);

    Page<CustomerJpaEntity> findAllByCustomerStatus(CustomerStatus status, Pageable pageable);

    long countByCustomerStatus(CustomerStatus status);

    @Query("""
            SELECT c FROM CustomerJpaEntity c
            WHERE LOWER(c.customerName) LIKE LOWER(CONCAT('%', :term, '%'))
               OR c.phoneNumber LIKE CONCAT('%', :term, '%')
               OR LOWER(c.customerCode) LIKE LOWER(CONCAT('%', :term, '%'))
            """)
    Page<CustomerJpaEntity> search(@Param("term") String term, Pageable pageable);
}
