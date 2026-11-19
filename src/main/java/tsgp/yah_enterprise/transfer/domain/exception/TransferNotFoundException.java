package tsgp.yah_enterprise.transfer.domain.exception;

/** 404: the transfer does not exist. */
public class TransferNotFoundException extends TransferDomainException {
    public TransferNotFoundException(java.util.UUID transferId) {
        super("Transfer not found: " + transferId);
    }
}
