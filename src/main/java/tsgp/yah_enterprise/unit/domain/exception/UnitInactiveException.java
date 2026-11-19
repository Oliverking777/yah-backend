package tsgp.yah_enterprise.unit.domain.exception;

import java.util.UUID;

/** Thrown when an inactive unit is used where an active one is required (for example a new product unit). */
public class UnitInactiveException extends UnitDomainException {
    public UnitInactiveException(UUID unitId) {
        super("Unit is inactive: " + unitId);
    }
}
