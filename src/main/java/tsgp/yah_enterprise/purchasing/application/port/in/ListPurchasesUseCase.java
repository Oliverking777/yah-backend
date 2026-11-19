package tsgp.yah_enterprise.purchasing.application.port.in;

import tsgp.yah_enterprise.purchasing.application.dto.*;
import java.util.UUID;

public interface ListPurchasesUseCase { PurchasePageResult listPurchases(PurchasePageRequest request); }
