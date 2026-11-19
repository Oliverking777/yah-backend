package tsgp.yah_enterprise.dashboard.infrastructure.integration;

import tsgp.yah_enterprise.dashboard.domain.port.SectionExecutor;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/** Runs the dashboard sources in parallel. Timeout: yah.dashboard.source-timeout-millis (default 3000). */
@Component
public class DashboardSectionExecutor implements SectionExecutor {

    private final ExecutorService pool = Executors.newFixedThreadPool(8, r -> {
        Thread t = new Thread(r, "dashboard-source");
        t.setDaemon(true);
        return t;
    });
    private final long timeoutMillis;

    public DashboardSectionExecutor(
            @org.springframework.beans.factory.annotation.Value("${yah.dashboard.source-timeout-millis:3000}")
            long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public <T> Future<T> submit(Supplier<T> task) {
        return pool.submit(task::get);
    }

    @Override
    public long timeoutMillis() {
        return timeoutMillis;
    }

    @PreDestroy
    void shutdown() {
        pool.shutdownNow();
    }
}
