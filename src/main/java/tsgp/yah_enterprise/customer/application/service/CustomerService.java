package tsgp.yah_enterprise.customer.application.service;

import tsgp.yah_enterprise.customer.domain.port.CustomerAuditPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tsgp.yah_enterprise.customer.application.dto.*;
import tsgp.yah_enterprise.customer.application.port.in.*;
import tsgp.yah_enterprise.customer.domain.exception.CustomerNotFoundException;
import tsgp.yah_enterprise.customer.domain.model.Customer;
import tsgp.yah_enterprise.customer.domain.repository.CustomerRepository;
import tsgp.yah_enterprise.customer.domain.valueobject.CustomerCode;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService implements
        CreateCustomerUseCase,
        GetCustomerUseCase,
        GetCustomersByIdsUseCase,
        ListCustomersUseCase,
        UpdateCustomerUseCase,
        ChangeCustomerStatusUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerAuditPort audit;
    private final CustomerCodeSequence customerCodeSequence;

    @Override
    public CustomerResult createCustomer(CreateCustomerCommand command) {
        CustomerCode code = CustomerCode.fromSequence(customerCodeSequence.next());
        Customer customer = Customer.create(code, command.customerName(), command.phoneNumber(),
                command.email(), command.address(), command.customerType());
        return CustomerResult.from(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResult getCustomer(UUID customerId) {
        return CustomerResult.from(find(customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResult> getCustomersByIds(Collection<UUID> customerIds) {
        return customerRepository.findAllByIds(customerIds).stream()
                .map(CustomerResult::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerPageResult listCustomers(CustomerPageRequest request) {
        boolean searching = request.search() != null && !request.search().isBlank();
        List<CustomerResult> content = searching
                ? customerRepository.search(request.search(), request.page(), request.size())
                        .stream().map(CustomerResult::from).toList()
                : customerRepository.findAll(request.page(), request.size(), request.status())
                        .stream().map(CustomerResult::from).toList();
        long total = searching
                ? customerRepository.countSearch(request.search())
                : customerRepository.count(request.status());
        return CustomerPageResult.of(content, request.page(), request.size(), total);
    }

    @Override
    public CustomerResult updateCustomer(UUID customerId, UpdateCustomerCommand command) {
        Customer customer = find(customerId);
        if (command.customerName() != null) customer.changeName(command.customerName());
        customer.changeContact(command.phoneNumber(), command.email(), command.address());
        return CustomerResult.from(customerRepository.save(customer));
    }

    @Override
    public CustomerResult activateCustomer(UUID customerId) {
        Customer customer = find(customerId);
        customer.activate();
        Customer saved = customerRepository.save(customer);
        audit.record(new CustomerAuditPort.AuditRecord(null, "CUSTOMER.STATUS_CHANGED", "CUSTOMER", customerId, null, null,
                List.of(new CustomerAuditPort.Change("customerStatus", "INACTIVE", "ACTIVE"))));
        return CustomerResult.from(saved);
    }

    @Override
    public CustomerResult deactivateCustomer(UUID customerId) {
        Customer customer = find(customerId);
        customer.deactivate();
        Customer saved = customerRepository.save(customer);
        audit.record(new CustomerAuditPort.AuditRecord(null, "CUSTOMER.STATUS_CHANGED", "CUSTOMER", customerId, null, null,
                List.of(new CustomerAuditPort.Change("customerStatus", "ACTIVE", "INACTIVE"))));
        return CustomerResult.from(saved);
    }

    private Customer find(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }
}
