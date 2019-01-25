package com.scaythe.status.module;

import com.scaythe.status.input.ClickEvent;
import com.scaythe.status.module.config.ModuleConfigTemplate;
import com.scaythe.status.write.ModuleData;
import com.scaythe.status.write.ModuleDataImmutable;
import reactor.core.publisher.Flux;

import java.util.Optional;

public abstract class Module {

    private final String name;
    private final String instance;

    public Module(ModuleConfigTemplate config) {
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

    public final Flux<ModuleData> stream() {
        return data().startWith()
                .startWith(ModuleDataImmutable.builder().fullText("").name(name()).build());
    }

    protected abstract Flux<ModuleData> data();

    public void event(ClickEvent event) {
    }
}
