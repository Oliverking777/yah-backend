package tsgp.yah_enterprise.reports.domain.port;

import tsgp.yah_enterprise.reports.domain.model.*;
import tsgp.yah_enterprise.reports.domain.valueobject.*;

import java.util.*;

/** Names for ids, in batch. A name that does not resolve is missing from the map: it never fails a report. */
public interface ReportNameDirectoryPort {

    Map<UUID, String> customerNames(Collection<UUID> customerIds);

    Map<UUID, String> productNames(Collection<UUID> productIds);

    Map<UUID, String> supplierNames(Collection<UUID> supplierIds);
}
