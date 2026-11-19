package tsgp.yah_enterprise.location.domain.exception;


public class LocationCodeAlreadyExistsException extends LocationDomainException {
    public LocationCodeAlreadyExistsException(String code) {
        super("A location with code \"" + code + "\" already exists");
    }
}