package tsgp.yah_enterprise.purchasing.application.dto;

import java.util.*;
import java.time.*;
import tsgp.yah_enterprise.purchasing.domain.model.*;

public record PurchaseLineCommand(UUID productId, UUID unitId, int quantity, long unitPrice) { }
