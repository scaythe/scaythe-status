package com.scaythe.status.module;

import com.scaythe.status.module.config.SamplingModuleConfig;
import com.scaythe.status.write.ModuleData;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import reactor.core.publisher.Flux;

abstract class SamplingModule<T> extends Module {

  private final Duration sampleRate;
  private final Collection<T> samples;

  SamplingModule(SamplingModuleConfig config) {
    super(config.moduleConfig());

    sampleRate = config.sampleRate().orElseGet(this::defaultSampleRate);
    samples = new LimitedQueue<>(config.size().orElseGet(this::defaultSize));
  }

  @Override
  public ModuleData data() {
    return Flux.interval(sampleRate)
        .map(l -> sample())
        .doOnNext(samples::add)
        .map(s -> reduce(List.copyOf(samples)));
  }

  public abstract Duration defaultSampleRate();

  public abstract int defaultSize();

  public abstract T sample();

  public abstract ModuleData reduce(List<T> samples);
}
