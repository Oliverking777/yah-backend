package tsgp.yah_enterprise.purchasing.application.dto;

import java.util.*;
import java.time.*;
import tsgp.yah_enterprise.purchasing.domain.model.*;

public record CreatePurchaseCommand(UUID purchaseId, UUID supplierId, UUID destinationLocationId, LocalDate expectedDeliveryDate, String note, List<PurchaseLineCommand> items, PurchaseActor actor) { }
