package tsgp.yah_enterprise.purchasing.application.dto;

import java.util.*;
import java.time.*;
import tsgp.yah_enterprise.purchasing.domain.model.*;

/** Used by cancel. */
public record PurchaseReasonCommand(UUID purchaseId, String reason, PurchaseActor actor) { }
