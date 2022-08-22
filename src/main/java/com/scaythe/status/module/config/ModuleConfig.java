package com.scaythe.status.module.config;

import java.util.Optional;

public record ModuleConfig(Optional<String> name, Optional<String> instance) {

  public static ModuleConfig defaults() {
    return new ModuleConfig(Optional.empty(), Optional.empty());
  }
}
