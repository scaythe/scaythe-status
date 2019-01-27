package com.scaythe.status.input;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

@Component
public class EventReader {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Gson json;

    public EventReader(Gson json) {
        this.json = json;
    }

    public Flux<ClickEvent> events() {
        return Flux.create(this::read).subscribeOn(Schedulers.parallel());
    }

    private void read(FluxSink<ClickEvent> sink) {
        try (JsonReader reader = json.newJsonReader(new InputStreamReader(System.in))) {
            reader.beginArray();

            while (!sink.isCancelled() && reader.hasNext()) {
                readEvent(reader).ifPresent(sink::next);
            }
        } catch (IOException e) {
            sink.error(e);
        }

        sink.complete();
    }

    private Optional<ClickEvent> readEvent(JsonReader reader) {
        try {
            return Optional.ofNullable(json.fromJson(reader, ClickEvent.class));
        } catch (JsonIOException e) {
            log.error("problem reading json input : {} : {}",
                    e.getClass().getName(),
                    e.getMessage());
            log.error("", e);
        } catch (JsonSyntaxException e) {
            log.error("malformed json input : {} : {}", e.getClass().getName(), e.getMessage());
            log.error("", e);
        }

        return Optional.empty();
    }
}
