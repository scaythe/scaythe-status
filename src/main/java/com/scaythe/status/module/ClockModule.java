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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClockModule extends Module {

  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private boolean started = false;

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
    log.atDebug().log("producing output");
    // add a little time to avoid sometimes generating output for the previous second
    output(ModuleData.of(format(Instant.now().plusMillis(100)), name()));
  }

  private Duration getDelayToNextSecond() {
    Instant now = Instant.now();
    // a bit more than one second to avoid sometimes generating two outputs in the same second
    Instant nextSecond = now.plusMillis(1100).truncatedTo(ChronoUnit.SECONDS);
    return Duration.between(now, nextSecond);
  }

  private synchronized void scheduleNextExecution(Duration delay) {
    if (Thread.interrupted()) return;

    executor.schedule(
        logError(this::produceOutputAndScheduleNextExecution),
        delay.toMillis(),
        TimeUnit.MILLISECONDS);
  }

  @Override
  public synchronized void start() {
    if (executor.isShutdown()) throw new IllegalStateException("already stopped");
    if (started) throw new IllegalStateException("already started");

    scheduleNextExecution(Duration.ZERO);
    started = true;
  }

  @Override
  public synchronized void stop() {
    executor.shutdownNow();
  }

  private String format(Instant instant) {
    return formatter.format(instant.atZone(ZoneId.systemDefault()));
  }
}
