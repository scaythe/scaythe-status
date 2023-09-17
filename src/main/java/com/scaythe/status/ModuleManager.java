package com.scaythe.status;

import com.scaythe.status.input.ClickEvent;
import com.scaythe.status.module.ClockModule;
import com.scaythe.status.module.Module;
import com.scaythe.status.module.SystemModule;
import com.scaythe.status.module.config.SamplingModuleConfig;
import com.scaythe.status.write.ModuleData;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import lombok.Getter;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class ModuleManager implements SmartLifecycle {

  @Getter
  private final BlockingQueue<List<ModuleData>> combinedDataQueue = new ArrayBlockingQueue<>(10);

  private final List<Module> modules;
  private boolean running = false;

  public ModuleManager() {
    SamplingModuleConfig config = SamplingModuleConfig.defaults();

    ModuleDataCombiner combiner = new ModuleDataCombiner(combinedDataQueue);
    modules =
        List.of(
            //            new SpotifyModule(config.moduleConfig(), combiner.getNextModuleOutput()),
            new SystemModule(config, combiner.getNextModuleOutput()),
            new ClockModule(config.moduleConfig(), combiner.getNextModuleOutput()));
  }

  public void dispatchEvent(ClickEvent event) {
    modules.stream()
        .filter(m -> m.name().equals(event.name()))
        .filter(m -> Objects.equals(m.instance(), event.instance()))
        .forEach(m -> m.event(event));
  }

  @Override
  public synchronized void start() {
    modules.forEach(Module::start);
    running = true;
  }

  @Override
  public synchronized void stop() {
    modules.forEach(Module::stop);
    running = false;
  }

  @Override
  public synchronized boolean isRunning() {
    return running;
  }
}
