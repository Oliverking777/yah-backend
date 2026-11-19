package tsgp.yah_enterprise.supplier.application.port.in;
import tsgp.yah_enterprise.supplier.application.dto.SupplierResult;
import java.util.UUID;
public interface ChangeSupplierStatusUseCase {
    SupplierResult activateSupplier(UUID supplierId);
    SupplierResult deactivateSupplier(UUID supplierId);
}
