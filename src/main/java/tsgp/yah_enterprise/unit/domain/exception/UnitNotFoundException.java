package tsgp.yah_enterprise.unit.domain.exception;

import java.util.UUID;

public class UnitNotFoundException extends UnitDomainException {
    public UnitNotFoundException(UUID unitId) {
        super("Unit not found: " + unitId);
    }
}
