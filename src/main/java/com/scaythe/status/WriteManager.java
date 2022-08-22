package com.scaythe.status;

import com.scaythe.status.module.Module;
import com.scaythe.status.write.ModuleData;
import com.scaythe.status.write.StatusWriter;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Component
public class WriteManager implements SmartLifecycle {

  private final ModuleManager moduleManager;
  private final StatusWriter writer;

  private Disposable disposable = null;

  public WriteManager(ModuleManager moduleManager, StatusWriter writer) {
    this.moduleManager = moduleManager;
    this.writer = writer;
  }

  @Override
  public void start() {
    disposable =
        Flux.combineLatest(data(), this::combine)
            .subscribeOn(Schedulers.parallel())
            .sample(Duration.ofMillis(10))
            .distinctUntilChanged()
            .doOnNext(writer::write)
            .onBackpressureLatest()
            .subscribe();
  }

  private List<Flux<ModuleData>> data() {
    return moduleManager.modules().stream().map(this::data).collect(Collectors.toList());
  }

  private Flux<ModuleData> data(Module module) {
    return module.data().startWith(ModuleData.empty(module.name()));
  }

  private List<ModuleData> combine(Object[] dataArray) {
    return Stream.of(dataArray)
        .filter(ModuleData.class::isInstance)
        .map(ModuleData.class::cast)
        .collect(Collectors.toList());
  }

  @Override
  public void stop() {
    if (disposable != null) {
      disposable.dispose();
      disposable = null;
    }
  }

  @Override
  public boolean isRunning() {
    return disposable != null;
  }
}
