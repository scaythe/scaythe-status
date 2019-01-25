package com.scaythe.status.input;

import com.google.gson.Gson;
import com.scaythe.status.ModuleManager;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class EventReader implements SmartLifecycle {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson json;
    private final ModuleManager moduleManager;

    public EventReader(Gson json, ModuleManager moduleManager) {
        this.json = json;
        this.moduleManager = moduleManager;
    }

    @Override
    public void start() {
        executor.execute(this::read);
    }

    @Override
    public void stop() {
        executor.shutdownNow();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    private void read() {
        try (Scanner scan = new Scanner(System.in)) {
            while (!Thread.interrupted() && scan.hasNextLine()) {
                String line = scan.nextLine();

                match(line.replaceFirst("^,", "")).ifPresent(moduleManager::event);
            }
        }
    }

    private Optional<ClickEvent> match(String line) {
        try {
            ClickEvent event = json.fromJson(line, ClickEvent.class);

            if (event.name().isPresent()) {
                return Optional.of(event);
            }
        } catch (Exception e) {
        }

        return Optional.empty();
    }
}
