package tsgp.yah_enterprise.supplier.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tsgp.yah_enterprise.supplier.domain.model.SupplierStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
@Getter @Setter @NoArgsConstructor
public class SupplierJpaEntity {

    @Id
    @Column(name = "supplier_id", nullable = false, updatable = false)
    private UUID supplierId;

    @Column(name = "supplier_code", nullable = false, updatable = false, length = 20)
    private String supplierCode;

    @Column(name = "supplier_name", nullable = false, length = 150)
    private String supplierName;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "address", length = 300)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "supplier_status", nullable = false, length = 20)
    private SupplierStatus supplierStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
