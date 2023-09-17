package com.scaythe.status.module;

import com.scaythe.status.input.ClickEvent;
import com.scaythe.status.module.config.ModuleConfig;
import com.scaythe.status.write.ModuleData;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Getter
public abstract class Module {
  private final String name;
  private final @Nullable String instance;

  @Getter(AccessLevel.NONE)
  private final Consumer<ModuleData> output;

  protected Module(ModuleConfig config, Consumer<ModuleData> output) {
    this.name = Objects.requireNonNullElseGet(config.name(), this::defaultName);
    this.instance = config.instance();
    this.output = output;
  }

  public abstract String defaultName();

  public void event(ClickEvent event) {}

  protected void output(ModuleData data) {
    output.accept(data);
  }

  public abstract void start();

  public abstract void pause();

  public abstract void stop();
}
