package tsgp.yah_enterprise.location.domain.exception;


public class InvalidLocationTypeException extends LocationDomainException {
    public InvalidLocationTypeException(String value) {
        super("Invalid location type: \"" + value + "\". Allowed values: WAREHOUSE, STORE");
    }
}