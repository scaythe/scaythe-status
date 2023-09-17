package com.scaythe.status.module;

import static com.scaythe.status.ScaytheStatusApp.logError;

import com.scaythe.status.module.config.ModuleConfig;
import com.scaythe.status.write.ModuleData;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public class ClockModule extends Module {

  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private @Nullable ScheduledFuture<?> nextExecution = null;

  public ClockModule(ModuleConfig config, Consumer<ModuleData> output) {
    super(config, output);
  }

  @Override
  public String defaultName() {
    return "clock";
  }

  public void produceOutputAndScheduleNextExecution() {
    produceOutput();
    scheduleNextExecution(getDelayToNextSecond());
  }

  private void produceOutput() {
    output(ModuleData.of(format(Instant.now()), name()));
  }

  private Duration getDelayToNextSecond() {
    Instant now = Instant.now();
    Instant nextSecond = now.plusSeconds(1).truncatedTo(ChronoUnit.SECONDS);
    return Duration.between(now, nextSecond);
  }

  private synchronized void scheduleNextExecution(Duration delay) {
    if (Thread.interrupted()) return;

    nextExecution =
        executor.schedule(
            logError(this::produceOutputAndScheduleNextExecution),
            delay.toMillis(),
            TimeUnit.MILLISECONDS);
  }

  @Override
  public synchronized void start() {
    if (executor.isShutdown()) throw new IllegalStateException("already stopped");
    if (nextExecution != null) throw new IllegalStateException("already running");

    scheduleNextExecution(Duration.ZERO);
  }

  @Override
  public synchronized void pause() {
    if (executor.isShutdown()) throw new IllegalStateException("already stopped");
    if (nextExecution == null) throw new IllegalStateException("not running");

    nextExecution.cancel(true);
    nextExecution = null;
  }

  @Override
  public synchronized void stop() {
    executor.shutdownNow();
  }

  private String format(Instant instant) {
    return formatter.format(instant.atZone(ZoneId.systemDefault()));
  }
}
