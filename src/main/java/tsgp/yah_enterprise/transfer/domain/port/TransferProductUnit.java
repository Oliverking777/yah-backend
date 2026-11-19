package tsgp.yah_enterprise.transfer.domain.port;

import java.util.UUID;

/** One unit of a product as Transfer sees it. Its own record, so Transfer never imports Product's classes. */
public record TransferProductUnit(UUID unitId, String unitCode, String unitName, boolean active,
                                  int conversionFactor) { }
