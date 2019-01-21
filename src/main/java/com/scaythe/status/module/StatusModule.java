package com.scaythe.status.module;

import com.scaythe.status.input.ClickEvent;
import org.springframework.context.Lifecycle;

import java.util.Objects;
import java.util.Optional;

public abstract class StatusModule implements Lifecycle {

    private final String name;
    private final String instance;
    private final String markup;
    private final Runnable update;
    private ModuleData data = new ModuleData("");

    public StatusModule(String name, String instance, String markup, Runnable update) {
        this.name = name;
        this.instance = instance;
        this.markup = markup;
        this.update = update;
    }

    protected void update(ModuleData data) {
        if (!Objects.equals(this.data, data)) {
            this.data = data;
            update.run();
        }
    }

    public String name() {
        return name;
    }

    public Optional<String> instance() {
        return Optional.ofNullable(instance);
    }

    public Optional<String> markup() {
        return Optional.ofNullable(markup);
    }

    public ModuleData data() {
        return data;
    }

    public void event(ClickEvent event) {
    }
}
