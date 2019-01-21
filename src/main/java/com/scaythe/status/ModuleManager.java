package com.scaythe.status;

import com.scaythe.status.input.ClickEvent;
import com.scaythe.status.module.ClockModule;
import com.scaythe.status.module.SpotifyModule;
import com.scaythe.status.module.StatusModule;
import com.scaythe.status.module.SystemModule;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ModuleManager implements SmartLifecycle {

    private final List<StatusModule> modules = new ArrayList<>();
    private final StatusWriter writer;

    private boolean running = false;

    public ModuleManager(StatusWriter writer, SystemInfo systemInfo) {
        this.writer = writer;

        modules.add(new SpotifyModule(this::update));
        modules.add(new SystemModule(this::update));
        modules.add(new ClockModule(this::update));
    }

    @Override
    public void start() {
        System.out.println("{\"version\": 1, \"click_events\": true}");
        System.out.println("[");

        update();

        modules.forEach(StatusModule::start);

        running = true;
    }

    @Override
    public void stop() {
        running = false;

        modules.forEach(StatusModule::stop);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void update() {
        writer.write(modules);
    }

    public void event(ClickEvent event) {
        modules.stream()
                .filter(m -> m.name().equals(event.name()))
                .filter(m -> Objects.equals(event.instance(), m.instance()))
                .forEach(m -> m.event(event));
    }
}
