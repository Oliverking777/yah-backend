package tsgp.yah_enterprise.payment.application.port.in;

import tsgp.yah_enterprise.payment.application.dto.*;
import java.util.*;

public interface AllocatePaymentUseCase {
    List<AllocationResult> allocatePayment(AllocatePaymentCommand command);
}
