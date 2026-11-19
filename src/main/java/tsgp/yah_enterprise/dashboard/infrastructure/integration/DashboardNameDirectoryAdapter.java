package tsgp.yah_enterprise.dashboard.infrastructure.integration;

import tsgp.yah_enterprise.dashboard.domain.port.DashboardNameDirectoryPort;
import org.springframework.stereotype.Component;
import tsgp.yah_enterprise.product.application.port.in.GetProductUseCase;

import java.util.*;

@Component
public class DashboardNameDirectoryAdapter implements DashboardNameDirectoryPort {

    private final GetProductUseCase products;

    public DashboardNameDirectoryAdapter(GetProductUseCase products) {
        this.products = products;
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
}
