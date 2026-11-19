package tsgp.yah_enterprise.customer.domain.model;

import lombok.Getter;
import tsgp.yah_enterprise.customer.domain.exception.CustomerAlreadyActiveException;
import tsgp.yah_enterprise.customer.domain.exception.CustomerAlreadyInactiveException;
import tsgp.yah_enterprise.customer.domain.exception.CustomerInactiveException;
import tsgp.yah_enterprise.customer.domain.exception.InvalidCustomerException;
import tsgp.yah_enterprise.customer.domain.exception.WalkInCustomerProtectedException;
import tsgp.yah_enterprise.customer.domain.valueobject.CustomerCode;
import tsgp.yah_enterprise.customer.domain.valueobject.CustomerName;
import tsgp.yah_enterprise.customer.domain.valueobject.Email;
import tsgp.yah_enterprise.customer.domain.valueobject.PhoneNumber;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Customer {

    private static final int MAX_ADDRESS_LENGTH = 300;

    private final UUID customerId;
    private final CustomerCode customerCode;
    private CustomerName customerName;
    private PhoneNumber phoneNumber;
    private Email email;
    private String address;
    private final CustomerType customerType;
    private CustomerStatus customerStatus;
    private final boolean walkIn;
    private final Instant createdAt;
    private Instant updatedAt;

    private Customer(UUID customerId, CustomerCode customerCode, CustomerName customerName,
                     PhoneNumber phoneNumber, Email email, String address,
                     CustomerType customerType, CustomerStatus customerStatus,
                     boolean walkIn, Instant createdAt, Instant updatedAt) {
        this.customerId = Objects.requireNonNull(customerId);
        this.customerCode = Objects.requireNonNull(customerCode);
        this.customerName = Objects.requireNonNull(customerName);
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = normaliseAddress(address);
        this.customerType = Objects.requireNonNull(customerType);
        this.customerStatus = Objects.requireNonNull(customerStatus);
        this.walkIn = walkIn;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Customer create(CustomerCode code, String name, String phone, String email,
                                  String address, CustomerType type) {
        PhoneNumber phoneNumber = resolvePhone(phone, type);
        Email emailObj = email != null && !email.isBlank() ? Email.of(email) : null;
        Instant now = Instant.now();
        return new Customer(UUID.randomUUID(), code, CustomerName.of(name),
                phoneNumber, emailObj, address, type, CustomerStatus.ACTIVE, false, now, now);
    }

    public static Customer reconstitute(UUID customerId, CustomerCode customerCode, String customerName,
                                        String phoneNumber, String email, String address,
                                        CustomerType customerType, CustomerStatus customerStatus,
                                        boolean walkIn, Instant createdAt, Instant updatedAt) {
        PhoneNumber phone = phoneNumber != null ? PhoneNumber.of(phoneNumber) : null;
        Email emailObj = email != null ? Email.of(email) : null;
        return new Customer(customerId, customerCode, CustomerName.of(customerName),
                phone, emailObj, address, customerType, customerStatus, walkIn, createdAt, updatedAt);
    }

    public void changeName(String newName) {
        guardWalkIn();
        this.customerName = CustomerName.of(newName);
        touch();
    }

    public void changeContact(String phone, String email, String address) {
        guardWalkIn();
        if (phone != null) {
            this.phoneNumber = phone.isBlank() ? null : PhoneNumber.of(phone);
        }
        if (email != null) {
            this.email = email.isBlank() ? null : Email.of(email);
        }
        if (address != null) {
            this.address = normaliseAddress(address);
        }
        touch();
    }

    public void activate() {
        if (customerStatus == CustomerStatus.ACTIVE) {
            throw new CustomerAlreadyActiveException(customerId);
        }
        this.customerStatus = CustomerStatus.ACTIVE;
        touch();
    }

    public void deactivate() {
        guardWalkIn();
        if (customerStatus == CustomerStatus.INACTIVE) {
            throw new CustomerAlreadyInactiveException(customerId);
        }
        this.customerStatus = CustomerStatus.INACTIVE;
        touch();
    }

    public boolean isActive() {
        return customerStatus == CustomerStatus.ACTIVE;
    }

    public void ensureActive() {
        if (!isActive()) {
            throw new CustomerInactiveException(customerId);
        }
    }

    // ---- helpers ----

    private void guardWalkIn() {
        if (walkIn) {
            throw new WalkInCustomerProtectedException();
        }
    }

    private static PhoneNumber resolvePhone(String phone, CustomerType type) {
        if (phone == null || phone.isBlank()) {
            if (type == CustomerType.BUSINESS) {
                throw new InvalidCustomerException("Phone number is required for business customers");
            }
            return null;
        }
        return PhoneNumber.of(phone);
    }

    private static String normaliseAddress(String address) {
        if (address == null) return null;
        String trimmed = address.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.length() > MAX_ADDRESS_LENGTH) {
            throw new InvalidCustomerException("Address must not exceed " + MAX_ADDRESS_LENGTH + " characters");
        }
        return trimmed;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Customer c && customerId.equals(c.customerId));
    }

    @Override
    public int hashCode() {
        return customerId.hashCode();
    }
}
