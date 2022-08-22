package com.scaythe.status.module.config;

import java.time.Duration;
import java.util.Optional;

public record SamplingModuleConfig(
    ModuleConfig moduleConfig, Optional<Duration> sampleRate, Optional<Integer> size) {

  public static SamplingModuleConfig defaults() {
    return new SamplingModuleConfig(ModuleConfig.defaults(), Optional.empty(), Optional.empty());
  }

  public static SamplingModuleConfig defaults(ModuleConfig moduleConfig) {
    return new SamplingModuleConfig(moduleConfig, Optional.empty(), Optional.empty());
  }
}
