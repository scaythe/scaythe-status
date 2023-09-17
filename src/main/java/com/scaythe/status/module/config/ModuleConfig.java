package com.scaythe.status.module.config;

import org.jspecify.annotations.Nullable;

public record ModuleConfig(@Nullable String name, @Nullable String instance) {

  public static ModuleConfig defaults() {
    return new ModuleConfig(null, null);
  }
}
