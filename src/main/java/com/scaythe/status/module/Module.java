package com.scaythe.status.module;

import com.scaythe.status.input.ClickEvent;
import com.scaythe.status.module.config.ModuleConfig;
import com.scaythe.status.write.ModuleData;
import java.util.Optional;
import reactor.core.publisher.Flux;

public abstract class Module {

  private final String name;
  private final String instance;

  public Module(ModuleConfig config) {
    this.name = config.name().orElseGet(this::defaultName);
    this.instance = config.instance().orElse(null);
  }

  public String name() {
    return name;
  }

  public Optional<String> instance() {
    return Optional.ofNullable(instance);
  }

  public abstract String defaultName();

  public abstract Flux<ModuleData> data();

  public void event(ClickEvent event) {}
}
