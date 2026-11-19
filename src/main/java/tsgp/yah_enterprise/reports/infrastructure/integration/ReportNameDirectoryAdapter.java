package tsgp.yah_enterprise.reports.infrastructure.integration;

import tsgp.yah_enterprise.reports.domain.port.ReportNameDirectoryPort;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.customer.application.port.in.GetCustomersByIdsUseCase;
import tsgp.yah_enterprise.product.application.port.in.GetProductUseCase;
import tsgp.yah_enterprise.supplier.application.port.in.GetSuppliersByIdsUseCase;

import java.util.*;

/** Customer, product and supplier names through their use cases. Failures leave the name missing. */
@Component
public class ReportNameDirectoryAdapter implements ReportNameDirectoryPort {

    private final GetCustomersByIdsUseCase customers;
    private final GetProductUseCase products;
    private final GetSuppliersByIdsUseCase suppliers;

    public ReportNameDirectoryAdapter(GetCustomersByIdsUseCase customers, GetProductUseCase products,
                                      GetSuppliersByIdsUseCase suppliers) {
        this.customers = customers;
        this.products = products;
        this.suppliers = suppliers;
    }

    @Override
    public Map<UUID, String> customerNames(Collection<UUID> ids) {
        Map<UUID, String> out = new HashMap<>();
        if (ids.isEmpty()) {
            return out;
        }
        try {
            customers.getCustomersByIds(new HashSet<>(ids)).forEach(c -> out.put(c.customerId(), c.customerName()));
        } catch (RuntimeException ignored) {
            // names stay missing
        }
        return out;
    }

    @Override
    public Map<UUID, String> productNames(Collection<UUID> ids) {
        Map<UUID, String> out = new HashMap<>();
        for (UUID id : new HashSet<>(ids)) {
            try {
                out.put(id, products.getProduct(id).productName());
            } catch (RuntimeException ignored) {
                // name stays missing
            }
        }
        return out;
    }

    @Override
    public Map<UUID, String> supplierNames(Collection<UUID> ids) {
        Map<UUID, String> out = new HashMap<>();
        if (ids.isEmpty()) {
            return out;
        }
        try {
            suppliers.getSuppliersByIds(new HashSet<>(ids)).forEach(s -> out.put(s.supplierId(), s.supplierName()));
        } catch (RuntimeException ignored) {
            // names stay missing
        }
        return out;
    }
}
