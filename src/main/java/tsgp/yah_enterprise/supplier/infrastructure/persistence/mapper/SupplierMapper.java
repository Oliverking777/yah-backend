package tsgp.yah_enterprise.supplier.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.supplier.domain.model.Supplier;
import tsgp.yah_enterprise.supplier.domain.valueobject.SupplierCode;
import tsgp.yah_enterprise.supplier.infrastructure.persistence.entity.SupplierJpaEntity;

@Component
public class SupplierMapper {

    public Supplier toDomain(SupplierJpaEntity e) {
        return Supplier.reconstitute(
                e.getSupplierId(), SupplierCode.of(e.getSupplierCode()),
                e.getSupplierName(), e.getContactPerson(), e.getPhoneNumber(),
                e.getEmail(), e.getAddress(), e.getSupplierStatus(),
                e.getCreatedAt(), e.getUpdatedAt());
    }

    public SupplierJpaEntity toEntity(Supplier s) {
        SupplierJpaEntity e = new SupplierJpaEntity();
        e.setSupplierId(s.getSupplierId());
        e.setSupplierCode(s.getSupplierCode().value());
        e.setSupplierName(s.getSupplierName().value());
        e.setContactPerson(s.getContactPerson());
        e.setPhoneNumber(s.getPhoneNumber() != null ? s.getPhoneNumber().value() : null);
        e.setEmail(s.getEmail() != null ? s.getEmail().value() : null);
        e.setAddress(s.getAddress());
        e.setSupplierStatus(s.getSupplierStatus());
        e.setCreatedAt(s.getCreatedAt());
        e.setUpdatedAt(s.getUpdatedAt());
        return e;
    }
}
