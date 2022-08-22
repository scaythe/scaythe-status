package com.scaythe.status;

import com.scaythe.status.module.ClockModule;
import com.scaythe.status.module.Module;
import com.scaythe.status.module.SpotifyModule;
import com.scaythe.status.module.SystemModule;
import com.scaythe.status.module.config.SamplingModuleConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ModuleManager {

  private final List<Module> modules = new ArrayList<>();

  public ModuleManager() {
    SamplingModuleConfig config = SamplingModuleConfig.defaults();

    modules.add(new SpotifyModule(config.moduleConfig()));
    modules.add(new SystemModule(config));
    modules.add(new ClockModule(config));
  }

  public List<Module> modules() {
    return Collections.unmodifiableList(modules);
  }
}
