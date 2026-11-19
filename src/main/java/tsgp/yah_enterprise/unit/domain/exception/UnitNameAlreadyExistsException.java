package tsgp.yah_enterprise.unit.domain.exception;

import tsgp.yah_enterprise.unit.domain.valueobject.UnitName;

public class UnitNameAlreadyExistsException extends UnitDomainException {
    public UnitNameAlreadyExistsException(UnitName name) {
        super("A unit named \"" + name.value() + "\" already exists");
    }
}
