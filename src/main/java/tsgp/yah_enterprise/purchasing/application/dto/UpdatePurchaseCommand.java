package tsgp.yah_enterprise.purchasing.application.dto;

import java.util.*;
import java.time.*;
import tsgp.yah_enterprise.purchasing.domain.model.*;

public record UpdatePurchaseCommand(UUID purchaseId, UUID supplierId, UUID destinationLocationId, LocalDate expectedDeliveryDate, String note, PurchaseActor actor) { }
