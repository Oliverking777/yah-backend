package tsgp.yah_enterprise.transfer.domain.exception;

/** 409: an offline client reused an id that another user already created. */
public class TransferIdAlreadyUsedException extends TransferDomainException {
    public TransferIdAlreadyUsedException(java.util.UUID transferId) {
        super("Transfer id " + transferId + " already belongs to a transfer created by another user");
    }
}
