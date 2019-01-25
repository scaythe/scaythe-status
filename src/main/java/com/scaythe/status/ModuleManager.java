package com.scaythe.status;

import com.scaythe.status.input.ClickEvent;
import com.scaythe.status.module.ClockModule;
import com.scaythe.status.module.Module;
import com.scaythe.status.module.SpotifyModule;
import com.scaythe.status.module.SystemModule;
import com.scaythe.status.module.config.SamplingModuleConfig;
import com.scaythe.status.write.ModuleData;
import com.scaythe.status.write.StatusWriter;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ModuleManager implements SmartLifecycle {
    private final List<Module> modules = new ArrayList<>();
    private final StatusWriter writer;

    private Disposable disposable = null;

    public ModuleManager(StatusWriter writer) {
        this.writer = writer;

        SamplingModuleConfig config = SamplingModuleConfig.defaults();

        modules.add(new SpotifyModule(config));
        modules.add(new SystemModule(config));
        modules.add(new ClockModule(config));
    }

    @Override
    public void start() {
        disposable = Flux.combineLatest(modules.stream()
                .map(Module::stream)
                .collect(Collectors.toList()), this::combine)
                .distinctUntilChanged()
                .doOnNext(writer::write)
                .subscribe();
    }

    private List<ModuleData> combine(Object[] dataArray) {
        return Stream.of(dataArray).map(ModuleData.class::cast).collect(Collectors.toList());
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

    public void event(ClickEvent event) {
        modules.stream()
                .filter(m -> m.name().equals(event.name().orElse(null)))
                .filter(m -> Objects.equals(m.instance(), event.instance()))
                .forEach(m -> m.event(event));
    }
}
