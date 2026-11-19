package tsgp.yah_enterprise.sales.application.port.in;

import tsgp.yah_enterprise.sales.application.dto.*;
import java.util.*;

public interface CompleteSaleUseCase {
    SaleResult completeSale(CompleteSaleCommand command);
}
