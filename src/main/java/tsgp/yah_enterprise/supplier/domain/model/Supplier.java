package tsgp.yah_enterprise.supplier.domain.model;

import lombok.Getter;
import tsgp.yah_enterprise.supplier.domain.exception.*;
import tsgp.yah_enterprise.supplier.domain.valueobject.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Supplier {

    private static final int MAX_ADDRESS_LENGTH = 300;
    private static final int MAX_CONTACT_LENGTH = 100;

    private final UUID supplierId;
    private final SupplierCode supplierCode;
    private SupplierName supplierName;
    private String contactPerson;
    private PhoneNumber phoneNumber;
    private Email email;
    private String address;
    private SupplierStatus supplierStatus;
    private final Instant createdAt;
    private Instant updatedAt;

    private Supplier(UUID supplierId, SupplierCode supplierCode, SupplierName supplierName,
                     String contactPerson, PhoneNumber phoneNumber, Email email, String address,
                     SupplierStatus supplierStatus, Instant createdAt, Instant updatedAt) {
        this.supplierId = Objects.requireNonNull(supplierId);
        this.supplierCode = Objects.requireNonNull(supplierCode);
        this.supplierName = Objects.requireNonNull(supplierName);
        this.contactPerson = normaliseContact(contactPerson);
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = normaliseAddress(address);
        this.supplierStatus = Objects.requireNonNull(supplierStatus);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Supplier create(SupplierCode code, String name, String contactPerson,
                                  String phone, String email, String address) {
        PhoneNumber phoneNumber = phone != null && !phone.isBlank() ? PhoneNumber.of(phone) : null;
        Email emailObj = email != null && !email.isBlank() ? Email.of(email) : null;
        Instant now = Instant.now();
        return new Supplier(UUID.randomUUID(), code, SupplierName.of(name),
                contactPerson, phoneNumber, emailObj, address, SupplierStatus.ACTIVE, now, now);
    }

    public static Supplier reconstitute(UUID supplierId, SupplierCode supplierCode, String supplierName,
                                        String contactPerson, String phoneNumber, String email,
                                        String address, SupplierStatus supplierStatus,
                                        Instant createdAt, Instant updatedAt) {
        PhoneNumber phone = phoneNumber != null ? PhoneNumber.of(phoneNumber) : null;
        Email emailObj = email != null ? Email.of(email) : null;
        return new Supplier(supplierId, supplierCode, SupplierName.of(supplierName),
                contactPerson, phone, emailObj, address, supplierStatus, createdAt, updatedAt);
    }

    public void rename(String newName) {
        this.supplierName = SupplierName.of(newName);
        touch();
    }

    public void changeContact(String contactPerson, String phone, String email, String address) {
        if (contactPerson != null) this.contactPerson = normaliseContact(contactPerson);
        if (phone != null) this.phoneNumber = phone.isBlank() ? null : PhoneNumber.of(phone);
        if (email != null) this.email = email.isBlank() ? null : Email.of(email);
        if (address != null) this.address = normaliseAddress(address);
        touch();
    }

    public void activate() {
        if (supplierStatus == SupplierStatus.ACTIVE) throw new SupplierAlreadyActiveException(supplierId);
        this.supplierStatus = SupplierStatus.ACTIVE;
        touch();
    }

    public void deactivate() {
        if (supplierStatus == SupplierStatus.INACTIVE) throw new SupplierAlreadyInactiveException(supplierId);
        this.supplierStatus = SupplierStatus.INACTIVE;
        touch();
    }

    public boolean isActive() { return supplierStatus == SupplierStatus.ACTIVE; }

    public void ensureActive() {
        if (!isActive()) throw new SupplierInactiveException(supplierId);
    }

    private static String normaliseContact(String contact) {
        if (contact == null) return null;
        String trimmed = contact.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.length() > MAX_CONTACT_LENGTH)
            throw new InvalidSupplierException("Contact person must not exceed " + MAX_CONTACT_LENGTH + " characters");
        return trimmed;
    }

    private static String normaliseAddress(String address) {
        if (address == null) return null;
        String trimmed = address.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.length() > MAX_ADDRESS_LENGTH)
            throw new InvalidSupplierException("Address must not exceed " + MAX_ADDRESS_LENGTH + " characters");
        return trimmed;
    }

    private void touch() { this.updatedAt = Instant.now(); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Supplier s && supplierId.equals(s.supplierId));
    }

    @Override
    public int hashCode() { return supplierId.hashCode(); }
}
