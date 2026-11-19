package tsgp.yah_enterprise.dashboard.application.service;

import tsgp.yah_enterprise.dashboard.application.dto.DashboardActor;
import tsgp.yah_enterprise.dashboard.domain.exception.DashboardDomainException;
import tsgp.yah_enterprise.dashboard.domain.exception.DashboardSourceUnavailableException;
import tsgp.yah_enterprise.dashboard.domain.model.LocationRef;
import tsgp.yah_enterprise.dashboard.domain.model.ScopeInfo;
import tsgp.yah_enterprise.dashboard.domain.port.DashboardLocationScopePort;
import tsgp.yah_enterprise.dashboard.domain.port.SectionExecutor;
import tsgp.yah_enterprise.dashboard.domain.service.LocationScopePolicy;
import tsgp.yah_enterprise.dashboard.domain.valueobject.LocationScope;

import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/** Shared rules of the dashboard services: scope, sources that may fail, parallel sections. */
final class DashboardSupport {

    private final DashboardLocationScopePort locations;
    private final SectionExecutor executor;
    private final LocationScopePolicy policy = new LocationScopePolicy();

    DashboardSupport(DashboardLocationScopePort locations, SectionExecutor executor) {
        this.locations = locations;
        this.executor = executor;
    }

    LocationScope scope(DashboardActor actor, UUID requestedLocationId) {
        Set<UUID> assigned = actor.canViewAll() ? Set.of() : required(() -> locations.assignedLocationIds(actor.userId()));
        return policy.resolve(actor.canViewAll(), assigned, requestedLocationId);
    }

    /** For list endpoints: a failing source is a 503. */
    static <T> T required(Supplier<T> call) {
        try {
            return call.get();
        } catch (DashboardDomainException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new DashboardSourceUnavailableException("A dashboard source is unavailable, try again later");
        }
    }

    Map<UUID, String> names(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            return locations.locationNames(ids);
        } catch (RuntimeException e) {
            return Map.of();
        }
    }

    ScopeInfo scopeInfo(LocationScope scope) {
        if (scope.all()) {
            return new ScopeInfo("ALL", List.of());
        }
        Map<UUID, String> names = names(scope.locationIds());
        List<LocationRef> refs = scope.locationIds().stream().sorted()
                .map(id -> new LocationRef(id, names.get(id))).toList();
        return new ScopeInfo("LOCATIONS", refs);
    }

    <T> Future<T> submit(Supplier<T> task) {
        return executor.submit(task);
    }

    /** Waits for a section until the shared deadline. A timeout or failure gives null. */
    <T> T await(Future<T> future, long deadlineNanos) {
        try {
            long remaining = Math.max(1, (deadlineNanos - System.nanoTime()) / 1_000_000);
            return future.get(remaining, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            return null;
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }

    long deadline() {
        return System.nanoTime() + executor.timeoutMillis() * 1_000_000L;
    }

    static int pages(long total, int size) {
        return (int) ((total + size - 1) / size);
    }
}
