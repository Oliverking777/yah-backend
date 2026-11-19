package tsgp.yah_enterprise.supplier.application.port.in;
import tsgp.yah_enterprise.supplier.application.dto.*;
import java.util.UUID;
public interface UpdateSupplierUseCase { SupplierResult updateSupplier(UUID supplierId, UpdateSupplierCommand command); }
