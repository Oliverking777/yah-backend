package tsgp.yah_enterprise.dashboard.domain.service;

import tsgp.yah_enterprise.dashboard.domain.model.AlertSeverity;
import tsgp.yah_enterprise.dashboard.domain.model.AlertType;
import tsgp.yah_enterprise.dashboard.domain.valueobject.AlertThresholds;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/** When a business record becomes an alert, how severe it is, and what the message says. */
public class AlertPolicy {

    private final AlertThresholds t;

    public AlertPolicy(AlertThresholds thresholds) {
        this.t = thresholds;
    }

    /** Empty when the record is not (yet) an alert. */
    public Optional<AlertSeverity> severity(AlertType type, Instant since, Instant now) {
        Duration age = since == null ? Duration.ZERO : Duration.between(since, now);
        return switch (type) {
            case OUT_OF_STOCK -> Optional.of(AlertSeverity.CRITICAL);
            case LOW_STOCK, TRANSFER_DISCREPANCY, PURCHASE_DISCREPANCY -> Optional.of(AlertSeverity.WARNING);
            case ORDER_AWAITING_VALIDATION -> Optional.of(escalate(age, t.orderWarning()));
            case CREDIT_REQUEST_PENDING -> Optional.of(escalate(age, t.creditWarning()));
            case STOCK_ADJUSTMENT_PENDING -> Optional.of(escalate(age, t.adjustmentWarning()));
            case TRANSFER_NOT_RECEIVED -> age.compareTo(t.transferReceiptCritical()) >= 0
                    ? Optional.of(AlertSeverity.CRITICAL) : after(age, t.transferReceiptWarning());
            // since is the moment the delivery became late: nothing before that
            case PURCHASE_NOT_DELIVERED -> after(age, Duration.ZERO);
        };
    }

    private static AlertSeverity escalate(Duration age, Duration warningAfter) {
        return age.compareTo(warningAfter) >= 0 ? AlertSeverity.WARNING : AlertSeverity.INFO;
    }

    private static Optional<AlertSeverity> after(Duration age, Duration warningAfter) {
        return age.compareTo(warningAfter) >= 0 ? Optional.of(AlertSeverity.WARNING) : Optional.empty();
    }

    /** label is the human reference (a number, a product name); where is a location name or null. */
    public String message(AlertType type, String label, Instant since, Instant now, String where) {
        String at = where == null ? "" : " at " + where;
        String ago = since == null ? "" : ago(Duration.between(since, now));
        return switch (type) {
            case OUT_OF_STOCK -> label + " is out of stock" + at;
            case LOW_STOCK -> label + " is low on stock" + at;
            case ORDER_AWAITING_VALIDATION -> "Order " + label + " waiting for validation for " + ago + at;
            case CREDIT_REQUEST_PENDING -> "Credit request on sale " + label + " waiting for a decision for " + ago + at;
            case STOCK_ADJUSTMENT_PENDING -> "Stock adjustment " + label + " waiting for approval for " + ago + at;
            case TRANSFER_NOT_RECEIVED -> "Transfer " + label + " sent " + ago + " ago and not received";
            case TRANSFER_DISCREPANCY -> "Transfer " + label + " received with a discrepancy " + ago + " ago";
            case PURCHASE_NOT_DELIVERED -> "Purchase " + label + " overdue by " + ago + at;
            case PURCHASE_DISCREPANCY -> "Purchase " + label + " delivered with rejected or missing goods " + ago + " ago" + at;
        };
    }

    static String ago(Duration d) {
        long hours = Math.max(0, d.toHours());
        if (hours < 1) {
            return "less than an hour";
        }
        if (hours < 168) {
            return hours + (hours == 1 ? " hour" : " hours");
        }
        return d.toDays() + " days";
    }
}
