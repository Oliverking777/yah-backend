package tsgp.yah_enterprise.customer.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsgp.yah_enterprise.customer.domain.model.CustomerStatus;
import tsgp.yah_enterprise.customer.domain.model.CustomerType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class CustomerJpaEntity {

    @Id
    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "customer_code", nullable = false, updatable = false, length = 20)
    private String customerCode;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "address", length = 300)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, updatable = false, length = 20)
    private CustomerType customerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_status", nullable = false, length = 20)
    private CustomerStatus customerStatus;

    @Column(name = "walk_in", nullable = false)
    private boolean walkIn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
