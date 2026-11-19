package tsgp.yah_enterprise.payment.application.port.in;

import tsgp.yah_enterprise.payment.application.dto.*;
import java.util.*;

public interface DecideCreditUseCase {
    CreditApprovalResult approveCredit(ApproveCreditCommand command);
    CreditApprovalResult rejectCredit(RejectCreditCommand command);
}
