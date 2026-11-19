package tsgp.yah_enterprise.dashboard.domain.port;

import tsgp.yah_enterprise.dashboard.domain.valueobject.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/** Runs the sources in parallel; each result is awaited for at most timeoutMillis. */
public interface SectionExecutor {

    <T> Future<T> submit(Supplier<T> task);

    long timeoutMillis();
}
