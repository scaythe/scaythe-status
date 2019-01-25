package com.scaythe.status.input;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.scaythe.status.ModuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class EventReader implements SmartLifecycle {

    private final Logger log = LoggerFactory.getLogger(getClass());

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
        try (JsonReader reader = json.newJsonReader(new InputStreamReader(System.in))) {
            reader.beginArray();

            while (reader.hasNext()) {
                readEvent(reader).ifPresent(moduleManager::event);
            }
        } catch (IOException e) {
            log.error("problem reading stdin : {} : {}", e.getClass().getName(), e.getMessage());
            log.error("", e);
        }
    }

    private Optional<ClickEvent> readEvent(JsonReader reader) {
        try {
            return Optional.ofNullable(json.fromJson(reader, ClickEvent.class));
        } catch (JsonIOException e) {
            log.error("problem reading json input : {} : {}", e.getClass().getName(), e.getMessage());
            log.error("", e);
        } catch (JsonSyntaxException e) {
            log.error("malformed json input : {} : {}", e.getClass().getName(), e.getMessage());
            log.error("", e);
        }

        return Optional.empty();
    }
}
