package tsgp.yah_enterprise.unit.domain.exception;

import tsgp.yah_enterprise.unit.domain.valueobject.UnitCode;

public class UnitCodeAlreadyExistsException extends UnitDomainException {
    public UnitCodeAlreadyExistsException(UnitCode code) {
        super("A unit with the code \"" + code.value() + "\" already exists");
    }
}
