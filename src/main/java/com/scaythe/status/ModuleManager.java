package com.scaythe.status;

import com.scaythe.status.module.ClockModule;
import com.scaythe.status.module.Module;
import com.scaythe.status.module.SpotifyModule;
import com.scaythe.status.module.SystemModule;
import com.scaythe.status.module.config.SamplingModuleConfig;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ModuleManager {

  private final List<Module> modules;

  public ModuleManager() {
    SamplingModuleConfig config = SamplingModuleConfig.defaults();

    modules =
        List.of(
            new SpotifyModule(config.moduleConfig()),
            new SystemModule(config),
            new ClockModule(config));
  }

  public List<Module> modules() {
    return modules;
  }
}
