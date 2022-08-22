package com.scaythe.status.module;

import lombok.Builder;

public record SystemData(double cpu, double memory, double swap, long netDown, long netUp) {
  @Builder
  public SystemData {
    // TODO remove when no longer needed for intellij lombok plugin, move @Builder to top level
  }
}
