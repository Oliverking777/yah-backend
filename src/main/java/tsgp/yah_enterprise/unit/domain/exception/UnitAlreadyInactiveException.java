package tsgp.yah_enterprise.unit.domain.exception;

import java.util.UUID;

public class UnitAlreadyInactiveException extends UnitDomainException {
    public UnitAlreadyInactiveException(UUID unitId) {
        super("Unit is already inactive: " + unitId);
    }
}
