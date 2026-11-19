package tsgp.yah_enterprise.reports.domain.valueobject;

import tsgp.yah_enterprise.reports.domain.exception.InvalidReportPeriodException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/** Local dates, both included, in the business time zone. At most 366 days. */
public record ReportPeriod(LocalDate from, LocalDate to, ZoneId zone) {

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Africa/Douala");
    public static final int MAX_DAYS = 366;

    public ReportPeriod {
        if (from == null || to == null) {
            throw new InvalidReportPeriodException("A period needs a from date and a to date");
        }
        if (from.isAfter(to)) {
            throw new InvalidReportPeriodException("The from date cannot be after the to date");
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_DAYS) {
            throw new InvalidReportPeriodException("A period can cover at most " + MAX_DAYS + " days");
        }
        zone = zone == null ? BUSINESS_ZONE : zone;
    }

    public static ReportPeriod of(LocalDate from, LocalDate to) {
        return new ReportPeriod(from, to, BUSINESS_ZONE);
    }

    /** First instant of the from date. */
    public Instant start() {
        return from.atStartOfDay(zone).toInstant();
    }

    /** First instant after the to date (exclusive end). */
    public Instant end() {
        return to.plusDays(1).atStartOfDay(zone).toInstant();
    }
}
