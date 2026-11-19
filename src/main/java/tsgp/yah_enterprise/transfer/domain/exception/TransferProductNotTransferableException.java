package tsgp.yah_enterprise.transfer.domain.exception;

/** 409: a product or unit is missing or inactive. */
public class TransferProductNotTransferableException extends TransferDomainException {
    public TransferProductNotTransferableException(String message) {
        super(message);
    }
}
