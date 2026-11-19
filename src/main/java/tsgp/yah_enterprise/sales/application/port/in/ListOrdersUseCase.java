package tsgp.yah_enterprise.sales.application.port.in;

import tsgp.yah_enterprise.sales.application.dto.*;
import java.util.*;

public interface ListOrdersUseCase {
    OrderPageResult listOrders(OrderPageRequest request);
}
