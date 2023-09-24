package com.scaythe.status.module;

import static com.scaythe.status.ScaytheStatusApp.logError;

import com.scaythe.status.module.config.SamplingModuleConfig;
import com.scaythe.status.write.ModuleData;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class SamplingModule<T> extends Module {
  private final Duration sampleRate;
  private final Queue<T> samples;

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private boolean started = false;

  SamplingModule(SamplingModuleConfig config, Consumer<ModuleData> output) {
    super(config.moduleConfig(), output);

    sampleRate = Objects.requireNonNullElseGet(config.sampleRate(), this::defaultSampleRate);
    samples = new LimitedQueue<>(Objects.requireNonNullElseGet(config.size(), this::defaultSize));
  }

  private void submitNextOutput() {
    executor.submit(this::produceOutput);
  }

  private void produceOutput() {
    T sample = sample();
    samples.add(sample);
    output(reduce(List.copyOf(samples)));
  }

  @Override
  public void start() {
    if (executor.isShutdown()) throw new IllegalStateException("already stopped");
    if (started) throw new IllegalStateException("already started");

    executor.scheduleWithFixedDelay(
        logError(this::submitNextOutput), 0, sampleRate.toMillis(), TimeUnit.MILLISECONDS);
    started = true;
  }

  @Override
  public void stop() {
    executor.shutdown();
  }

  public abstract Duration defaultSampleRate();

  public abstract int defaultSize();

  public abstract T sample();

  public abstract ModuleData reduce(List<T> samples);
}
