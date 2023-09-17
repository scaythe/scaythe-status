package com.scaythe.status.module.config;

import java.time.Duration;
import org.jspecify.annotations.Nullable;

public record SamplingModuleConfig(
    ModuleConfig moduleConfig, @Nullable Duration sampleRate, @Nullable Integer size) {

  public static SamplingModuleConfig defaults() {
    return new SamplingModuleConfig(ModuleConfig.defaults(), null, null);
  }
}
