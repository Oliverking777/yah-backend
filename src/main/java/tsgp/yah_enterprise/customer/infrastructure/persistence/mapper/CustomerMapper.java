package tsgp.yah_enterprise.customer.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.customer.domain.model.Customer;
import tsgp.yah_enterprise.customer.domain.valueobject.CustomerCode;
import tsgp.yah_enterprise.customer.infrastructure.persistence.entity.CustomerJpaEntity;

@Component
public class CustomerMapper {

    public Customer toDomain(CustomerJpaEntity e) {
        return Customer.reconstitute(
                e.getCustomerId(),
                CustomerCode.of(e.getCustomerCode()),
                e.getCustomerName(),
                e.getPhoneNumber(),
                e.getEmail(),
                e.getAddress(),
                e.getCustomerType(),
                e.getCustomerStatus(),
                e.isWalkIn(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }

    public CustomerJpaEntity toEntity(Customer c) {
        CustomerJpaEntity e = new CustomerJpaEntity();
        e.setCustomerId(c.getCustomerId());
        e.setCustomerCode(c.getCustomerCode().value());
        e.setCustomerName(c.getCustomerName().value());
        e.setPhoneNumber(c.getPhoneNumber() != null ? c.getPhoneNumber().value() : null);
        e.setEmail(c.getEmail() != null ? c.getEmail().value() : null);
        e.setAddress(c.getAddress());
        e.setCustomerType(c.getCustomerType());
        e.setCustomerStatus(c.getCustomerStatus());
        e.setWalkIn(c.isWalkIn());
        e.setCreatedAt(c.getCreatedAt());
        e.setUpdatedAt(c.getUpdatedAt());
        return e;
    }
}
