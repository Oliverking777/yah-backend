package tsgp.yah_enterprise.supplier.application.port.in;
import tsgp.yah_enterprise.supplier.application.dto.SupplierResult;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
public interface GetSuppliersByIdsUseCase { List<SupplierResult> getSuppliersByIds(Collection<UUID> supplierIds); }
