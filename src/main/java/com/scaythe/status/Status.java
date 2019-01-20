package com.scaythe.status;

import com.scaythe.status.module.StatusModule;
import com.scaythe.status.module.ModuleData;
import com.scaythe.status.module.SpotifyModule;
import com.scaythe.status.module.TimeModule;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class Status implements SmartLifecycle {
    private final List<StatusModule> modules = new ArrayList<>();

    private boolean running = false;

    public Status() {
        modules.add(new SpotifyModule(this::update));
        modules.add(new TimeModule(this::update));
    }

    @Override
    public void start() {
        System.out.println("{\"version\": 1}");
        System.out.println("[");

        update();

        modules.forEach(StatusModule::run);

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
        System.out.println(message());
    }

    private String message() {
        return "[" + modules() + "],";
    }

    private String modules() {
        return modules.stream().map(this::module).collect(Collectors.joining(","));
    }

    private String module(StatusModule module) {
        return "{" + text(module.data()) + color(module.data()).map(c -> "," + c).orElse("") + "}";
    }

    private String text(ModuleData data) {
        return entry("full_text", data.text());
    }

    private Optional<String> color(ModuleData data) {
        return data.color().map(c -> entry("color", c));
    }

    private String entry(String header, String value) {
        return MessageFormat.format("\"{0}\": \"{1}\"", header, value);
    }
}
