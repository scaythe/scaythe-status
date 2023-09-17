package com.scaythe.status.module;

import static com.scaythe.status.ScaytheStatusApp.logError;

import com.scaythe.status.module.config.SamplingModuleConfig;
import com.scaythe.status.write.ModuleData;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

abstract class SamplingModule<T> extends Module {
  private final Duration sampleRate;
  private final List<T> samples;

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private @Nullable ScheduledFuture<?> execution = null;

  SamplingModule(SamplingModuleConfig config, Consumer<ModuleData> output) {
    super(config.moduleConfig(), output);

    sampleRate = Objects.requireNonNullElseGet(config.sampleRate(), this::defaultSampleRate);
    samples = new LimitedQueue<>(Objects.requireNonNullElseGet(config.size(), this::defaultSize));
  }

  private void produceOutput() {
    T sample = sample();
    samples.add(sample);
    output(reduce(List.copyOf(samples)));
  }

  @Override
  public void start() {
    if (executor.isShutdown()) throw new IllegalStateException("already stopped");
    if (execution != null) throw new IllegalStateException("already running");

    execution =
        executor.scheduleAtFixedRate(
            logError(this::produceOutput), 0, sampleRate.toMillis(), TimeUnit.MILLISECONDS);
  }

  @Override
  public void pause() {
    if (executor.isShutdown()) throw new IllegalStateException("already stopped");
    if (execution == null) throw new IllegalStateException("not running");

    execution.cancel(true);
    execution = null;
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
