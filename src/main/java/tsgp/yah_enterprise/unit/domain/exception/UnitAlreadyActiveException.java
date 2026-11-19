package tsgp.yah_enterprise.unit.domain.exception;

import java.util.UUID;

public class UnitAlreadyActiveException extends UnitDomainException {
    public UnitAlreadyActiveException(UUID unitId) {
        super("Unit is already active: " + unitId);
    }
}
