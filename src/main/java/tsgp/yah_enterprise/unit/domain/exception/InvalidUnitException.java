package tsgp.yah_enterprise.unit.domain.exception;

/** A unit value (name, code, status filter) is invalid. */
public class InvalidUnitException extends UnitDomainException {
    public InvalidUnitException(String message) {
        super(message);
    }
}
