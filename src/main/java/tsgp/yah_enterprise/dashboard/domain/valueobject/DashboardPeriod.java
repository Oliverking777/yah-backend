package tsgp.yah_enterprise.dashboard.domain.valueobject;

import tsgp.yah_enterprise.dashboard.domain.exception.InvalidDashboardRequestException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/** Local dates, both included, in the business time zone. At most 366 days. */
public record DashboardPeriod(LocalDate from, LocalDate to, ZoneId zone) {

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Africa/Douala");
    public static final int MAX_DAYS = 366;

    public DashboardPeriod {
        if (from == null || to == null) {
            throw new InvalidDashboardRequestException("A period needs a from date and a to date");
        }
        if (from.isAfter(to)) {
            throw new InvalidDashboardRequestException("The from date cannot be after the to date");
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_DAYS) {
            throw new InvalidDashboardRequestException("A period can cover at most " + MAX_DAYS + " days");
        }
        zone = zone == null ? BUSINESS_ZONE : zone;
    }

    public static DashboardPeriod of(LocalDate from, LocalDate to) {
        return new DashboardPeriod(from, to, BUSINESS_ZONE);
    }

    public static DashboardPeriod day(LocalDate day) {
        return of(day, day);
    }

    public Instant start() {
        return from.atStartOfDay(zone).toInstant();
    }

    /** First instant after the to date (exclusive end). */
    public Instant end() {
        return to.plusDays(1).atStartOfDay(zone).toInstant();
    }
}
